package com.nomadspot.backend.common.security.jwt.provider;

import com.nomadspot.backend.common.security.constant.SecurityConst;
import com.nomadspot.backend.common.security.jwt.properties.JwtProperties;
import com.nomadspot.backend.common.security.model.CustomUserDetails;
import com.nomadspot.backend.domain.user.model.UserRole;
import com.nomadspot.backend.infra.redis.constant.RedisConst;
import com.nomadspot.backend.infra.redis.dao.RedisRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * PackageName : com.nomadspot.backend.common.security.jwt.provider
 * FileName    : JwtProvider
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@Component
@Slf4j
public class JwtProvider {

    private final String    issuer;
    private final SecretKey accessTokenKey;
    private final SecretKey refreshTokenKey;
    private final long      accessTokenExpirationMillis;
    private final long      refreshTokenExpirationMillis;

    private final RedisRepository redisRepository;

    public JwtProvider(final JwtProperties jwtProperties, final RedisRepository redisRepository) {
        issuer = jwtProperties.getIssuer();
        accessTokenKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getAccessTokenSecret()));
        refreshTokenKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getRefreshTokenSecret()));
        accessTokenExpirationMillis = jwtProperties.getAccessTokenExpirationSeconds() * 1000L;
        refreshTokenExpirationMillis = jwtProperties.getRefreshTokenExpirationSeconds() * 1000L;
        this.redisRepository = redisRepository;
    }

    /**
     * AccessToken 또는 RefreshToken 생성
     *
     * @param authentication 인증 정보
     * @param isRefreshToken RefreshToken 인지 여부(false 시 AccessToken, true 시 RefreshToken)
     * @return JWT AccessToken 또는 RefreshToken
     */
    public String generateToken(final Authentication authentication, final boolean isRefreshToken) {
        CustomUserDetails userDetails = getCustomUserDetails(authentication);

        String id    = userDetails.getId().toString();
        String email = userDetails.getUsername();
        String role  = userDetails.getRole().name();

        Date now                   = new Date(System.currentTimeMillis());
        Date accessTokenExpiresIn  = new Date(now.getTime() + accessTokenExpirationMillis);
        Date refreshTokenExpiresIn = new Date(now.getTime() + refreshTokenExpirationMillis);

        String token = getJwtBuilder().subject(id)
                                      .claim(SecurityConst.JWT_USERNAME_KEY, email)
                                      .claim(SecurityConst.JWT_AUTHORITIES_KEY, role)
                                      .issuedAt(now)
                                      .expiration(isRefreshToken ? refreshTokenExpiresIn : accessTokenExpiresIn)
                                      .signWith(isRefreshToken ? refreshTokenKey : accessTokenKey, SIG.HS512)
                                      .compact();

        if (isRefreshToken) {
            String redisKey = "%s%s".formatted(RedisConst.JWT_REFRESH_TOKEN_PREFIX, token);
            redisRepository.setValue(redisKey, token, Duration.ofMillis(refreshTokenExpirationMillis));
        }

        return token;
    }

    /**
     * AccessToken 혹은 RefreshToken으로부터 인증 정보 조회
     *
     * @param token          AccessToken 또는 RefreshToken
     * @param isRefreshToken RefreshToken 인지 여부(false 시 AccessToken, true 시 RefreshToken)
     * @return 인증 정보
     */
    public Authentication getAuthenticationFromToken(final String token, final boolean isRefreshToken) {
        Claims claims = getClaims(token, isRefreshToken ? refreshTokenKey : accessTokenKey);

        UUID     id    = UUID.fromString(claims.getSubject());
        String   email = claims.get(SecurityConst.JWT_USERNAME_KEY, String.class);
        UserRole role  = UserRole.valueOf(claims.get(SecurityConst.JWT_AUTHORITIES_KEY, String.class));

        CustomUserDetails userDetails = CustomUserDetails.of(id, email, role);
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(role.getAuthority())
        );

        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    /**
     * AccessToken 또는 RefreshToken 유효성 검증
     *
     * @param token          AccessToken 또는 RefreshToken
     * @param isRefreshToken RefreshToken 인지 여부(false 시 AccessToken, true 시 RefreshToken)
     * @return 유효성 검증 결과
     */
    public boolean validateToken(final String token, final boolean isRefreshToken) {
        try {
            Jwts.parser()
                .verifyWith(isRefreshToken ? refreshTokenKey : accessTokenKey)
                .build()
                .parseSignedClaims(token);
            return true;
            // HACK: 예외 처리
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명", e);
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT", e);
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 JWT 형식", e);
        } catch (IllegalArgumentException e) {
            log.error("올바르지 않은 JWT 형식", e);
        }
        return false;
    }

    /**
     * AccessToken 또는 RefreshToken으로부터 사용자 ID 조회
     *
     * @param token          AccessToken 또는 RefreshToken
     * @param isRefreshToken RefreshToken 인지 여부(false 시 AccessToken, true 시 RefreshToken)
     * @return 회원 ID
     */
    public UUID getUserIdFromToken(final String token, final boolean isRefreshToken) {
        if (!validateToken(token, isRefreshToken)) return null;
        return UUID.fromString(getClaims(token, isRefreshToken ? refreshTokenKey : accessTokenKey).getSubject());
    }

    /**
     * AccessToken 또는 RefreshToken으로부터 남은 유효 시간 조회
     *
     * @param token          AccessToken 또는 RefreshToken
     * @param isRefreshToken RefreshToken 인지 여부(false 시 AccessToken, true 시 RefreshToken)
     * @return 남은 유효 시간(초)
     */
    public long getRemainingSeconds(final String token, final boolean isRefreshToken) {
        Claims claims = getClaims(token, isRefreshToken ? refreshTokenKey : accessTokenKey);
        return (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000L;
    }

    // ========================= 내부 메서드 =========================

    /**
     * 인증 정보(CustomUserDetails) 조회
     *
     * @param authentication 인증 정보(Authentication)
     * @return 인증 정보(CustomUserDetails)
     */
    private CustomUserDetails getCustomUserDetails(final Authentication authentication) {
        return (CustomUserDetails) authentication.getPrincipal();
    }

    /**
     * JwtBuilder 생성
     *
     * @return JwtBuilder
     */
    private JwtBuilder getJwtBuilder() {
        return Jwts.builder().issuer(issuer);
    }

    /**
     * JWT를 파싱하여 Claims 조회
     *
     * @param token AccessToken 또는 RefreshToken
     * @param key   암호화 키
     * @return Claims
     */
    private Claims getClaims(final String token, final SecretKey key) {
        try {
            return Jwts.parser()
                       .verifyWith(key)
                       .build()
                       .parseSignedClaims(token)
                       .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

}
