package com.timespot.backend.domain.user.service;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.response.ErrorCode;
import com.timespot.backend.common.security.dto.AuthRequestDto;
import com.timespot.backend.domain.user.dao.SocialConnectionRepository;
import com.timespot.backend.domain.user.dao.UserRepository;
import com.timespot.backend.domain.user.dto.UserRequestDto;
import com.timespot.backend.domain.user.dto.UserResponseDto;
import com.timespot.backend.domain.user.dto.UserResponseDto.UserInfoResponse;
import com.timespot.backend.domain.user.model.MapApi;
import com.timespot.backend.domain.user.model.NotificationTiming;
import com.timespot.backend.domain.user.model.ProviderType;
import com.timespot.backend.domain.user.model.SocialConnection;
import com.timespot.backend.domain.user.model.User;
import com.timespot.backend.infra.security.oauth.client.IdpTokenExchangeClient;
import com.timespot.backend.infra.security.oauth.constant.TokenType;
import com.timespot.backend.infra.security.oauth.dto.OAuthResponseDto.AppleTokenValidationResponse;
import com.timespot.backend.infra.security.oauth.dto.OAuthResponseDto.GoogleTokenValidationResponse;
import com.timespot.backend.infra.security.oauth.model.OAuthProfile;
import com.timespot.backend.infra.security.oauth.model.OAuthProfileFactory;
import com.timespot.backend.infra.security.oauth.validator.CustomOAuth2TokenValidator;
import io.jsonwebtoken.Claims;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PackageName : com.timespot.backend.domain.user.service
 * FileName    : UserServiceImpl
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository             userRepository;
    private final SocialConnectionRepository socialConnectionRepository;
    private final IdpTokenExchangeClient     idpTokenExchangeClient;
    private final CustomOAuth2TokenValidator tokenValidator;

    /**
     * 소셜 인증 정보를 사용하여 회원 정보 조회
     *
     * @param providerType   소셜 인증 제공자 유형
     * @param providerUserId 소셜 인증 제공자 식별자
     * @return 회원 엔티티(Optional)
     */
    @Override
    public Optional<User> findUserForSocialConnection(final ProviderType providerType, final String providerUserId) {
        Optional<SocialConnection> opSocialConnection = socialConnectionRepository.findByProviderTypeAndProviderId(
                providerType, providerUserId
        );
        return opSocialConnection.map(SocialConnection::getUser);
    }

    /**
     * 소셜 인증 정보를 사용하여 회원 정보 생성
     *
     * @param dto 신규 회원 가입 요청 DTO
     * @return 회원 엔티티
     */
    @Override
    @Transactional
    public User createUserForSocialConnection(final AuthRequestDto.OAuth2SignupRequest dto) {
        final ProviderType providerType = ProviderType.from(dto.getProvider());
        final MapApi       mapApi       = MapApi.from(dto.getMapApi());

        switch (providerType) {
            case APPLE -> {
                AppleTokenValidationResponse tokenValidationResponse = idpTokenExchangeClient.validationAppleAuthCode(
                        dto.getAuthCode()
                );
                return createNewUser(providerType,
                                     tokenValidationResponse.idToken(),
                                     tokenValidationResponse.refreshToken(),
                                     dto.getEmail(),
                                     dto.getNickname(),
                                     mapApi);
            }
            case GOOGLE -> {
                GoogleTokenValidationResponse tokenValidationResponse = idpTokenExchangeClient.validationGoogleAuthCode(
                        dto.getAuthCode()
                );
                return createNewUser(providerType,
                                     tokenValidationResponse.idToken(),
                                     tokenValidationResponse.refreshToken(),
                                     dto.getEmail(),
                                     dto.getNickname(),
                                     mapApi);
            }
            default -> throw new GlobalException(ErrorCode.SOCIAL_CONNECTION_PROVIDER_NOT_SUPPORTED);
        }
    }

    /**
     * 회원 ID로 소셜 연결 정보 조회
     *
     * @param userId 회원 ID
     * @return 소셜 연결 엔티티
     */
    @Override
    public SocialConnection findByUserId(final UUID userId) {
        return socialConnectionRepository.findByUserId(userId)
                                         .orElseThrow(() -> new GlobalException(ErrorCode.SOCIAL_CONNECTION_NOT_FOUND));
    }

    /**
     * ID로 회원 정보 조회
     *
     * @param id 회원 ID
     * @return 회원 정보 응답 DTO
     */
    @Override
    public UserInfoResponse findUserInfoById(final UUID id) {
        return userRepository.findUserInfoById(id).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 회원 정보 수정
     *
     * @param id  회원 ID
     * @param dto 회원 정보 수정 요청 DTO
     */
    @Override
    @Transactional
    public void updateUserInfo(final UUID id, final UserRequestDto.UserInfoUpdateRequest dto) {
        final MapApi mapApi = MapApi.from(dto.getMapApi());

        User user = userRepository.findById(id).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        user.updateNickname(dto.getNickname());
        user.updateMapApi(mapApi);
    }

    @Override
    public UserResponseDto.UserNotificationResponse findUserNotificationSettings(final UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
        return new UserResponseDto.UserNotificationResponse(toNotificationTimingCodes(user.getNotificationTimings()));
    }

    @Override
    @Transactional
    public void updateUserNotificationSettings(final UUID id, final UserRequestDto.UserNotificationUpdateRequest dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        Set<NotificationTiming> notificationTimings = parseNotificationTimings(dto.getNotificationTimings());
        validateNotificationTimings(notificationTimings);

        user.updateNotificationTimings(notificationTimings);
    }


    /**
     * ID로 회원 탈퇴
     *
     * @param id 회원 ID
     */
    @Override
    @Transactional
    public void withdraw(final UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
        SocialConnection socialConnection = socialConnectionRepository.findByUserId(id)
                                                                      .orElseThrow(() -> new GlobalException(
                                                                              ErrorCode.SOCIAL_CONNECTION_NOT_FOUND
                                                                      ));

        switch (socialConnection.getProviderType()) {
            case APPLE -> idpTokenExchangeClient.revokeAppleToken(TokenType.REFRESH_TOKEN,
                                                                  socialConnection.getIdpRefreshToken());
            case GOOGLE -> idpTokenExchangeClient.revokeGoogleToken(socialConnection.getIdpRefreshToken());
            default -> throw new GlobalException(ErrorCode.SOCIAL_CONNECTION_PROVIDER_NOT_SUPPORTED);
        }

        socialConnectionRepository.delete(socialConnection);
        userRepository.delete(user);
    }

    // ========================= 내부 메서드 =========================

    /**
     * 신규 회원 엔티티 생성
     *
     * @param providerType 소셜 인증 제공자 유형
     * @param idToken      소셜 ID 토큰
     * @param refreshToken 소셜 Refresh Token
     * @param nickname     닉네임
     * @param mapApi       지도 API 유형
     * @return 신규 회원 엔티티
     */
    private User createNewUser(final ProviderType providerType,
                               final String idToken,
                               final String refreshToken,
                               final String email,
                               final String nickname,
                               final MapApi mapApi) {
        Claims claims = tokenValidator.verifyAndParse(providerType.name(), idToken);

        OAuthProfile oAuthProfile = OAuthProfileFactory.getOAuthProfile(providerType.name(), claims);
        validateEmail(email);

        User user = userRepository.save(User.of(email, nickname, mapApi));
        socialConnectionRepository.save(
                SocialConnection.of(user, providerType, oAuthProfile.getProviderUserId(), refreshToken)
        );

        return user;
    }

    private Set<NotificationTiming> parseNotificationTimings(final List<String> timings) {
        if (timings == null || timings.isEmpty()) {
            return new LinkedHashSet<>();
        }

        return timings.stream().map(NotificationTiming::from).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private List<String> toNotificationTimingCodes(final Set<NotificationTiming> notificationTimings) {
        return notificationTimings.stream()
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .map(NotificationTiming::toValue)
                .toList();
    }

    private void validateNotificationTimings(final Set<NotificationTiming> notificationTimings) {
        if (notificationTimings.contains(NotificationTiming.NONE) && notificationTimings.size() > 1) {
            throw new GlobalException(ErrorCode.USER_NOTIFICATION_TIMING_INVALID_COMBINATION);
        }
    }

    /**
     * 이메일 검증
     *
     * @param email 이메일
     */
    private void validateEmail(final String email) {
        if (email == null || email.isBlank()) throw new GlobalException(ErrorCode.USER_EMAIL_REQUIRED);
        if (userRepository.existsByEmail(email)) throw new GlobalException(ErrorCode.USER_EMAIL_DUPLICATED);
    }

}
