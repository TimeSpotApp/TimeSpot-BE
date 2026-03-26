package com.timespot.backend.common.security.jwt.provider;

import static com.timespot.backend.common.security.constant.SecurityConst.JWT_AUTHORITIES_KEY;
import static com.timespot.backend.common.security.constant.SecurityConst.JWT_MAP_API_KEY;
import static com.timespot.backend.common.security.constant.SecurityConst.JWT_PROVIDER_KEY;
import static com.timespot.backend.common.security.constant.SecurityConst.JWT_USERNAME_KEY;
import static io.jsonwebtoken.Jwts.SIG.HS512;
import static io.jsonwebtoken.io.Decoders.BASE64;

import com.timespot.backend.common.security.jwt.provider.properties.JwtProperties;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.user.model.MapApi;
import com.timespot.backend.domain.user.model.ProviderType;
import com.timespot.backend.domain.user.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
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
 * PackageName : com.timespot.backend.common.security.jwt.provider
 * FileName    : JwtProvider
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description : JWT 토큰 생성 및 검증 컴포넌트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 * 26. 3. 24.    loadingKKamo21       중복 로직 제거 및 최적화 (헬퍼 메서드 통합)
 */
@Component
@Slf4j
public class JwtProvider {

    private final String    issuer;
    private final SecretKey accessTokenKey;
    private final SecretKey refreshTokenKey;
    private final long      accessTokenExpirationMillis;
    private final long      refreshTokenExpirationMillis;

    public JwtProvider(final JwtProperties jwtProperties) {
        issuer = jwtProperties.getIssuer();
        accessTokenKey = Keys.hmacShaKeyFor(BASE64.decode(jwtProperties.getAccessTokenSecret()));
        refreshTokenKey = Keys.hmacShaKeyFor(BASE64.decode(jwtProperties.getRefreshTokenSecret()));
        accessTokenExpirationMillis = jwtProperties.getAccessTokenExpirationSeconds() * 1000L;
        refreshTokenExpirationMillis = jwtProperties.getRefreshTokenExpirationSeconds() * 1000L;
    }

    /**
     * AccessToken 생성
     */
    public String generateAccessToken(final UUID userId,
                                      final String email,
                                      final ProviderType providerType,
                                      final MapApi mapApi,
                                      final UserRole role) {
        return generateToken(userId, email, providerType, mapApi, role, false);
    }

    /**
     * RefreshToken 생성
     */
    public String generateRefreshToken(final UUID userId,
                                       final String email,
                                       final ProviderType providerType,
                                       final MapApi mapApi,
                                       final UserRole role) {
        return generateToken(userId, email, providerType, mapApi, role, true);
    }

    /**
     * AccessToken 으로 인증 정보 조회
     */
    public Authentication getAuthenticationFromAccessToken(final String accessToken) {
        return getAuthenticationFromToken(accessToken, false);
    }

    /**
     * RefreshToken 으로 인증 정보 조회
     */
    public Authentication getAuthenticationFromRefreshToken(final String refreshToken) {
        return getAuthenticationFromToken(refreshToken, true);
    }

    /**
     * AccessToken 유효성 검증
     */
    public boolean validateAccessToken(final String accessToken) {
        return validateToken(accessToken, false);
    }

    /**
     * RefreshToken 유효성 검증
     */
    public boolean validateRefreshToken(final String refreshToken) {
        return validateToken(refreshToken, true);
    }

    /**
     * AccessToken 으로 회원 ID 조회
     */
    public UUID getUserIdFromAccessToken(final String accessToken) {
        return getUserIdFromToken(accessToken, false);
    }

    /**
     * RefreshToken 으로 회원 ID 조회
     */
    public UUID getUserIdFromRefreshToken(final String refreshToken) {
        return getUserIdFromToken(refreshToken, true);
    }

    /**
     * AccessToken 만료 시간 (초) 조회
     */
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMillis / 1000L;
    }

    /**
     * RefreshToken 만료 시간 (초) 조회
     */
    public long getRefreshTokenExpirationSeconds() {
        return refreshTokenExpirationMillis / 1000L;
    }

    /**
     * AccessToken 남은 유효 시간 (초) 조회
     */
    public long getRemainingSecondsFromAccessToken(final String accessToken) {
        return getRemainingSeconds(accessToken, false);
    }

    /**
     * RefreshToken 남은 유효 시간 (초) 조회
     */
    public long getRemainingSecondsFromRefreshToken(final String refreshToken) {
        return getRemainingSeconds(refreshToken, true);
    }

    // ========================= 내부 메서드 =========================

    /**
     * JWT 토큰 생성 (공통 로직)
     */
    private String generateToken(final UUID userId,
                                 final String email,
                                 final ProviderType providerType,
                                 final MapApi mapApi,
                                 final UserRole role,
                                 final boolean isRefreshToken) {
        Date now       = new Date(System.currentTimeMillis());
        Date expiresIn = new Date(now.getTime() + getExpirationMillis(isRefreshToken));

        return Jwts.builder()
                   .issuer(issuer)
                   .subject(userId.toString())
                   .claim(JWT_USERNAME_KEY, email)
                   .claim(JWT_PROVIDER_KEY, providerType.name())
                   .claim(JWT_MAP_API_KEY, mapApi.name())
                   .claim(JWT_AUTHORITIES_KEY, role.name())
                   .issuedAt(now)
                   .expiration(expiresIn)
                   .signWith(getSigningKey(isRefreshToken), HS512)
                   .compact();
    }

    /**
     * JWT 토큰으로 인증 정보 조회 (공통 로직)
     */
    private Authentication getAuthenticationFromToken(final String token, final boolean isRefreshToken) {
        Claims claims = getClaims(token, isRefreshToken);

        UUID         id           = UUID.fromString(claims.getSubject());
        String       email        = claims.get(JWT_USERNAME_KEY, String.class);
        ProviderType providerType = ProviderType.from(claims.get(JWT_PROVIDER_KEY, String.class));
        MapApi       mapApi       = MapApi.from(claims.get(JWT_MAP_API_KEY, String.class));
        UserRole     role         = UserRole.from(claims.get(JWT_AUTHORITIES_KEY, String.class));

        CustomUserDetails userDetails = CustomUserDetails.of(id, email, providerType, mapApi, role);
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(role.getAuthority())
        );

        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    /**
     * JWT 토큰 유효성 검증 (공통 로직)
     */
    private boolean validateToken(final String token, final boolean isRefreshToken) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey(isRefreshToken))
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명", e);
            throw e;
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT", e);
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 JWT 형식", e);
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("올바르지 않은 JWT 형식", e);
            throw e;
        }
    }

    /**
     * JWT 토큰으로 회원 ID 조회 (공통 로직)
     */
    private UUID getUserIdFromToken(final String token, final boolean isRefreshToken) {
        if (!validateToken(token, isRefreshToken)) return null;
        return UUID.fromString(getClaims(token, isRefreshToken).getSubject());
    }

    /**
     * JWT 토큰 남은 유효 시간 조회 (공통 로직)
     */
    private long getRemainingSeconds(final String token, final boolean isRefreshToken) {
        Claims claims = getClaims(token, isRefreshToken);
        return (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000L;
    }

    /**
     * 서명 키 조회
     */
    private SecretKey getSigningKey(final boolean isRefreshToken) {
        return isRefreshToken ? refreshTokenKey : accessTokenKey;
    }

    /**
     * 만료 시간 (밀리초) 조회
     */
    private long getExpirationMillis(final boolean isRefreshToken) {
        return isRefreshToken ? refreshTokenExpirationMillis : accessTokenExpirationMillis;
    }

    /**
     * JWT Claims 추출 (만료된 토큰도 처리)
     */
    private Claims getClaims(final String token, final boolean isRefreshToken) {
        try {
            return Jwts.parser()
                       .verifyWith(getSigningKey(isRefreshToken))
                       .build()
                       .parseSignedClaims(token)
                       .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

}
