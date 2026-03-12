package com.timespot.backend.common.security.service;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.response.ErrorCode;
import com.timespot.backend.common.security.dto.AuthRequestDto;
import com.timespot.backend.common.security.dto.AuthResponseDto;
import com.timespot.backend.common.security.dto.AuthResponseDto.TokenResponse;
import com.timespot.backend.common.security.jwt.provider.JwtProvider;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.user.model.ProviderType;
import com.timespot.backend.domain.user.model.User;
import com.timespot.backend.domain.user.service.UserService;
import com.timespot.backend.infra.redis.constant.RedisConst;
import com.timespot.backend.infra.redis.dao.RedisRepository;
import com.timespot.backend.infra.security.oauth.model.OAuthProfile;
import com.timespot.backend.infra.security.oauth.model.OAuthProfileFactory;
import com.timespot.backend.infra.security.oauth.validator.CustomOAuth2TokenValidator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * PackageName : com.timespot.backend.common.security.service
 * FileName    : AuthServiceImpl
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final ApplicationEventPublisher eventPublisher;

    private final RedisRepository redisRepository;

    private final JwtProvider                jwtProvider;
    private final CustomOAuth2TokenValidator tokenValidator;

    private final UserService userService;

    /**
     * 소셜 인증 제공자 정보를 활용한 로그인
     *
     * @param provider 소셜 인증 제공자 유형
     * @param dto      소셜 인증 제공자 데이터
     * @return 신규 토큰 응답 DTO
     */
    @Override
    public AuthResponseDto.TokenResponse login(final String provider, final AuthRequestDto.OAuth2LoginRequest dto) {
        Claims claims = tokenValidator.verifyAndParse(provider, dto.getIdToken());

        OAuthProfile oAuthProfile = OAuthProfileFactory.getOAuthProfile(provider, claims);
        ProviderType providerType = ProviderType.from(provider);

        String resolvedNickname = dto.getNickname() != null && !dto.getNickname().isBlank()
                                  ? dto.getNickname()
                                  : oAuthProfile.getNickname();

        User user = userService.findOrCreateUserForSocialConnection(
                providerType,
                oAuthProfile.getProviderUserId(),
                oAuthProfile.getEmail(),
                resolvedNickname,
                dto.getAuthCode()
        );

        String accessToken  = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId(), user.getEmail(), user.getRole());

        long accessTokenExpiresIn  = jwtProvider.getAccessTokenExpirationSeconds();
        long refreshTokenExpiresIn = jwtProvider.getRefreshTokenExpirationSeconds();

        redisRepository.setValue("%s%s".formatted(RedisConst.JWT_REFRESH_TOKEN_PREFIX, user.getId().toString()),
                                 refreshToken,
                                 Duration.ofSeconds(refreshTokenExpiresIn));

        return new AuthResponseDto.TokenResponse(accessToken,
                                                 accessTokenExpiresIn,
                                                 refreshToken,
                                                 refreshTokenExpiresIn);
    }

    /**
     * 로그아웃
     *
     * @param accessToken AccessToken
     */
    @Override
    public void logout(final String accessToken) {
        UUID userId = jwtProvider.getUserIdFromAccessToken(accessToken);

        if (userId != null) {
            String refreshTokenRedisKey = "%s%s".formatted(RedisConst.JWT_REFRESH_TOKEN_PREFIX, userId);
            redisRepository.deleteData(refreshTokenRedisKey);
        }

        long remainingSeconds = jwtProvider.getRemainingSecondsFromAccessToken(accessToken);
        if (remainingSeconds > 0) {
            String accessTokenBlacklistRedisKey = "%s%s".formatted(RedisConst.JWT_ACCESS_TOKEN_BLACKLIST_PREFIX,
                                                                   accessToken);
            redisRepository.setValue(accessTokenBlacklistRedisKey, "logout", Duration.ofSeconds(remainingSeconds));
        }
    }

    /**
     * RefreshToken을 사용하여 AccessToken 및 RefreshToken 갱신
     *
     * @param refreshToken RefreshToken
     * @return 갱신된 토큰 응답 DTO
     */
    @Override
    public TokenResponse refresh(final String refreshToken) {
        try {
            jwtProvider.validateRefreshToken(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new GlobalException(ErrorCode.USER_AUTH_REFRESH_TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.USER_AUTH_INVALID_REFRESH_TOKEN);
        }

        UUID   userId               = jwtProvider.getUserIdFromRefreshToken(refreshToken);
        String refreshTokenRedisKey = "%s%s".formatted(RedisConst.JWT_REFRESH_TOKEN_PREFIX, userId.toString());
        String savedToken           = redisRepository.getValue(refreshTokenRedisKey, String.class).orElse(null);

        if (savedToken == null || !savedToken.equals(refreshToken))
            throw new GlobalException(ErrorCode.USER_AUTH_INVALID_REFRESH_TOKEN);

        String graceRedisKey              = "%sgrace:%s".formatted(RedisConst.JWT_REFRESH_TOKEN_PREFIX, refreshToken);
        String newlyGeneratedTokenInGrace = redisRepository.getValue(graceRedisKey, String.class).orElse(null);

        if (newlyGeneratedTokenInGrace != null) {
            Authentication    authentication = jwtProvider.getAuthenticationFromRefreshToken(refreshToken);
            CustomUserDetails userDetails    = (CustomUserDetails) authentication.getPrincipal();

            return new AuthResponseDto.TokenResponse(
                    jwtProvider.generateAccessToken(userDetails.getId(),
                                                    userDetails.getUsername(),
                                                    userDetails.getRole()),
                    jwtProvider.getAccessTokenExpirationSeconds(),
                    newlyGeneratedTokenInGrace,
                    jwtProvider.getRefreshTokenExpirationSeconds()
            );
        }

        User user = userService.findById(userId);

        String newAccessToken  = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String newRefreshToken = jwtProvider.generateRefreshToken(user.getId(), user.getEmail(), user.getRole());

        redisRepository.setValue(graceRedisKey, newRefreshToken, Duration.ofSeconds(30));

        redisRepository.setValue(refreshTokenRedisKey,
                                 newRefreshToken,
                                 Duration.ofSeconds(jwtProvider.getRefreshTokenExpirationSeconds()));

        return new AuthResponseDto.TokenResponse(newAccessToken,
                                                 jwtProvider.getAccessTokenExpirationSeconds(),
                                                 newRefreshToken,
                                                 jwtProvider.getRefreshTokenExpirationSeconds());
    }

}
