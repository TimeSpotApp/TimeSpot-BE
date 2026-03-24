package com.timespot.backend.common.security.service;

import static com.timespot.backend.common.response.ErrorCode.USER_AUTH_INVALID_REFRESH_TOKEN;
import static com.timespot.backend.common.response.ErrorCode.USER_AUTH_REFRESH_TOKEN_EXPIRED;
import static com.timespot.backend.infra.redis.constant.RedisConst.JWT_ACCESS_TOKEN_BLACKLIST_PREFIX;
import static com.timespot.backend.infra.redis.constant.RedisConst.JWT_REFRESH_TOKEN_PREFIX;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.security.dto.AuthRequestDto.OAuth2LoginRequest;
import com.timespot.backend.common.security.dto.AuthRequestDto.OAuth2SignupRequest;
import com.timespot.backend.common.security.dto.AuthResponseDto;
import com.timespot.backend.common.security.dto.AuthResponseDto.AuthInfoResponse;
import com.timespot.backend.common.security.dto.AuthResponseDto.TokenInfoResponse;
import com.timespot.backend.common.security.jwt.provider.JwtProvider;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.user.model.ProviderType;
import com.timespot.backend.domain.user.model.SocialConnection;
import com.timespot.backend.domain.user.model.User;
import com.timespot.backend.domain.user.service.UserService;
import com.timespot.backend.infra.redis.dao.RedisRepository;
import com.timespot.backend.infra.security.oauth.model.OAuthProfile;
import com.timespot.backend.infra.security.oauth.model.OAuthProfileFactory;
import com.timespot.backend.infra.security.oauth.validator.CustomOAuth2TokenValidator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PackageName : com.timespot.backend.common.security.service
 * FileName    : AuthServiceImpl
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description : 인증 서비스 구현체
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final String LOGOUT_VALUE         = "logout";
    private static final long   GRACE_PERIOD_SECONDS = 30L;

    private final RedisRepository            redisRepository;
    private final JwtProvider                jwtProvider;
    private final CustomOAuth2TokenValidator tokenValidator;
    private final UserService                userService;

    /**
     * 소셜 인증 제공자 정보를 활용한 회원 가입
     *
     * @param dto 소셜 인증 제공자 데이터
     * @return 신규 인증 정보 응답 DTO
     */
    @Override
    @Transactional
    public AuthInfoResponse signup(final OAuth2SignupRequest dto) {
        final ProviderType providerType = ProviderType.from(dto.getProvider());
        User               user         = userService.createUserForSocialConnection(dto);
        return createAuthInfoResponse(user, providerType);
    }

    /**
     * 소셜 인증 제공자 정보를 활용한 로그인
     *
     * @param dto 소셜 인증 제공자 데이터
     * @return 신규 인증 정보 응답 DTO
     */
    @Override
    public AuthInfoResponse login(final OAuth2LoginRequest dto) {
        Claims claims = tokenValidator.verifyAndParse(dto.getProvider(), dto.getIdToken());

        OAuthProfile oAuthProfile = OAuthProfileFactory.getOAuthProfile(dto.getProvider(), claims);
        ProviderType providerType = ProviderType.from(dto.getProvider());

        return userService.findUserForSocialConnection(providerType, oAuthProfile.getProviderUserId())
                          .map(user -> createAuthInfoResponse(user, providerType))
                          .orElseGet(() -> new AuthInfoResponse(
                                  providerType,
                                  true,
                                  oAuthProfile.getEmail(),
                                  oAuthProfile.getNickname()
                          ));
    }

    /**
     * 로그아웃
     *
     * @param accessToken AccessToken
     */
    @Override
    public void logout(final String accessToken) {
        UUID userId = jwtProvider.getUserIdFromAccessToken(accessToken);

        deleteRefreshToken(userId);

        long remainingSeconds = jwtProvider.getRemainingSecondsFromAccessToken(accessToken);
        if (remainingSeconds > 0) blacklistAccessToken(accessToken, remainingSeconds);
    }

    /**
     * RefreshToken을 사용하여 AccessToken 및 RefreshToken 갱신
     *
     * @param refreshToken RefreshToken
     * @return 갱신된 인증 정보 응답 DTO
     */
    @Override
    public AuthResponseDto.TokenInfoResponse refresh(final String refreshToken) {
        validateRefreshToken(refreshToken);

        UUID   userId               = jwtProvider.getUserIdFromRefreshToken(refreshToken);
        String refreshTokenRedisKey = getRefreshTokenKey(userId);

        validateRefreshTokenMatch(refreshToken, refreshTokenRedisKey);

        String graceRedisKey = getGraceTokenKey(refreshToken);

        Optional<String> opGraceToken = redisRepository.getValue(graceRedisKey, String.class);

        return opGraceToken.map(token -> reissueWithGracePeriod(token, refreshToken))
                           .orElseGet(() -> reissueWithNewToken(userId, refreshTokenRedisKey, graceRedisKey));
    }

    /**
     * 회원 ID 기반 토큰 재발급
     *
     * @param userId 회원 ID
     * @return 재발급된 인증 정보 응답 DTO
     */
    @Override
    public AuthInfoResponse reissueTokenByUserId(final UUID userId) {
        SocialConnection socialConnection = userService.findByUserId(userId);
        return createAuthInfoResponse(socialConnection.getUser(), socialConnection.getProviderType());
    }

// ========================= 내부 메서드 =========================

    /**
     * 인증 정보 응답 DTO 생성
     *
     * @param user         회원 정보
     * @param providerType 소셜 인증 제공자 유형
     * @return 인증 정보 응답 DTO
     */
    private AuthInfoResponse createAuthInfoResponse(final User user, final ProviderType providerType) {
        String accessToken = jwtProvider.generateAccessToken(
                user.getId(), user.getEmail(), providerType, user.getMapApi(), user.getRole()
        );
        String refreshToken = jwtProvider.generateRefreshToken(
                user.getId(), user.getEmail(), providerType, user.getMapApi(), user.getRole()
        );

        long accessTokenExpiresIn  = jwtProvider.getAccessTokenExpirationSeconds();
        long refreshTokenExpiresIn = jwtProvider.getRefreshTokenExpirationSeconds();

        redisRepository.setValue("%s%s".formatted(JWT_REFRESH_TOKEN_PREFIX, user.getId().toString()),
                                 refreshToken,
                                 Duration.ofSeconds(refreshTokenExpiresIn));

        return new AuthInfoResponse(accessToken,
                                    accessTokenExpiresIn,
                                    refreshToken,
                                    refreshTokenExpiresIn,
                                    user.getMapApi(),
                                    providerType,
                                    false,
                                    user.getEmail(),
                                    user.getNickname());
    }

    /**
     * RefreshToken Redis 키 생성
     *
     * @param userId 회원 ID
     * @return RefreshToken Redis 키
     */
    private String getRefreshTokenKey(final UUID userId) {
        return JWT_REFRESH_TOKEN_PREFIX + userId;
    }

    /**
     * RefreshToken 삭제
     *
     * @param userId 회원 ID
     */
    private void deleteRefreshToken(final UUID userId) {
        if (userId != null) redisRepository.deleteData(getRefreshTokenKey(userId));
    }

    /**
     * AccessToken 블랙리스트 등록
     *
     * @param accessToken      AccessToken
     * @param remainingSeconds 남은 유효 시간 (초)
     */
    private void blacklistAccessToken(final String accessToken, final long remainingSeconds) {
        String accessTokenBlacklistRedisKey = JWT_ACCESS_TOKEN_BLACKLIST_PREFIX + accessToken;
        redisRepository.setValue(accessTokenBlacklistRedisKey, LOGOUT_VALUE, Duration.ofSeconds(remainingSeconds));
    }

    /**
     * RefreshToken 구조적 유효성 검증
     *
     * @param refreshToken RefreshToken
     * @throws GlobalException 유효성 검증 실패
     */
    private void validateRefreshToken(final String refreshToken) {
        try {
            jwtProvider.validateRefreshToken(refreshToken);
        } catch (ExpiredJwtException e) {
            log.error("RefreshToken 만료: {}", e.getMessage());
            throw new GlobalException(USER_AUTH_REFRESH_TOKEN_EXPIRED);
        } catch (SecurityException | MalformedJwtException e) {
            log.error("RefreshToken 서명 오류: {}", e.getMessage());
            throw new GlobalException(USER_AUTH_INVALID_REFRESH_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 RefreshToken 형식: {}", e.getMessage());
            throw new GlobalException(USER_AUTH_INVALID_REFRESH_TOKEN);
        } catch (IllegalArgumentException e) {
            log.error("잘못된 RefreshToken 형식: {}", e.getMessage());
            throw new GlobalException(USER_AUTH_INVALID_REFRESH_TOKEN);
        } catch (Exception e) {
            log.error("예상치 못한 RefreshToken 오류: {}", e.getMessage(), e);
            throw new GlobalException(USER_AUTH_INVALID_REFRESH_TOKEN);
        }
    }

    /**
     * RefreshToken 일치 여부 검증
     *
     * @param refreshToken         RefreshToken
     * @param refreshTokenRedisKey Redis 키
     * @throws GlobalException 토큰 불일치
     */
    private void validateRefreshTokenMatch(final String refreshToken, final String refreshTokenRedisKey) {
        String savedToken = redisRepository.getValue(refreshTokenRedisKey, String.class).orElse(null);

        if (savedToken == null || !savedToken.equals(refreshToken)) {
            log.error("RefreshToken 불일치: 저장된 토큰이 없음 또는 불일치");
            throw new GlobalException(USER_AUTH_INVALID_REFRESH_TOKEN);
        }
    }

    /**
     * Grace period 토큰 Redis 키 생성
     *
     * @param refreshToken RefreshToken
     * @return Grace period 토큰 Redis 키
     */
    private String getGraceTokenKey(final String refreshToken) {
        return "%sgrace:%s".formatted(JWT_REFRESH_TOKEN_PREFIX, refreshToken);
    }

    /**
     * Grace period를 사용한 토큰 재발급
     *
     * @param graceToken   Grace period 토큰
     * @param refreshToken RefreshToken
     * @return 토큰 정보 응답 DTO
     */
    private AuthResponseDto.TokenInfoResponse reissueWithGracePeriod(
            final String graceToken, final String refreshToken
    ) {
        Authentication    authentication = jwtProvider.getAuthenticationFromRefreshToken(refreshToken);
        CustomUserDetails userDetails    = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtProvider.generateAccessToken(
                userDetails.getId(), userDetails.getUsername(), userDetails.getProviderType(), userDetails.getMapApi(),
                userDetails.getRole()
        );

        return new TokenInfoResponse(
                accessToken,
                jwtProvider.getAccessTokenExpirationSeconds(),
                graceToken,
                jwtProvider.getRefreshTokenExpirationSeconds()
        );
    }

    /**
     * 신규 토큰 재발급 및 Redis 갱신
     *
     * @param userId               회원 ID
     * @param refreshTokenRedisKey RefreshToken Redis 키
     * @param graceRedisKey        Grace period 토큰 Redis 키
     * @return 토큰 정보 응답 DTO
     */
    private AuthResponseDto.TokenInfoResponse reissueWithNewToken(
            final UUID userId, final String refreshTokenRedisKey, final String graceRedisKey
    ) {
        SocialConnection socialConnection = userService.findByUserId(userId);
        User             user             = socialConnection.getUser();

        String newAccessToken = jwtProvider.generateAccessToken(
                user.getId(), user.getEmail(), socialConnection.getProviderType(), user.getMapApi(), user.getRole()
        );
        String newRefreshToken = jwtProvider.generateRefreshToken(
                user.getId(), user.getEmail(), socialConnection.getProviderType(), user.getMapApi(), user.getRole()
        );

        long accessTokenExpiresIn  = jwtProvider.getAccessTokenExpirationSeconds();
        long refreshTokenExpiresIn = jwtProvider.getRefreshTokenExpirationSeconds();

        // Grace period 설정 (30 초)
        redisRepository.setValue(graceRedisKey, newRefreshToken, Duration.ofSeconds(GRACE_PERIOD_SECONDS));
        // RefreshToken 갱신
        redisRepository.setValue(refreshTokenRedisKey, newRefreshToken, Duration.ofSeconds(refreshTokenExpiresIn));

        return new TokenInfoResponse(
                newAccessToken, accessTokenExpiresIn, newRefreshToken, refreshTokenExpiresIn
        );
    }

}
