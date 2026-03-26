package com.timespot.backend.domain.user.service;

import static com.timespot.backend.common.response.ErrorCode.SOCIAL_CONNECTION_NOT_FOUND;
import static com.timespot.backend.common.response.ErrorCode.SOCIAL_CONNECTION_PROVIDER_NOT_SUPPORTED;
import static com.timespot.backend.common.response.ErrorCode.USER_EMAIL_DUPLICATED;
import static com.timespot.backend.common.response.ErrorCode.USER_EMAIL_REQUIRED;
import static com.timespot.backend.common.response.ErrorCode.USER_NOT_FOUND;
import static com.timespot.backend.infra.security.oauth.constant.TokenType.REFRESH_TOKEN;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.response.ErrorCode;
import com.timespot.backend.common.security.dto.AuthRequestDto.OAuth2SignupRequest;
import com.timespot.backend.domain.user.dao.SocialConnectionRepository;
import com.timespot.backend.domain.user.dao.UserRepository;
import com.timespot.backend.domain.user.dto.UserRequestDto;
import com.timespot.backend.domain.user.dto.UserResponseDto;
import com.timespot.backend.domain.user.dto.UserRequestDto.UserInfoUpdateRequest;
import com.timespot.backend.domain.user.dto.UserResponseDto.UserInfoResponse;
import com.timespot.backend.domain.user.model.MapApi;
import com.timespot.backend.domain.user.model.NotificationTiming;
import com.timespot.backend.domain.user.model.ProviderType;
import com.timespot.backend.domain.user.model.SocialConnection;
import com.timespot.backend.domain.user.model.User;
import com.timespot.backend.infra.security.oauth.client.IdpTokenExchangeClient;
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
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PackageName : com.timespot.backend.domain.user.service
 * FileName    : UserServiceImpl
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description : 사용자 도메인 서비스 구현체
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 * 26. 3. 24.    loadingKKamo21       중복 로직 제거 및 최적화 (전략 패턴 활용)
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
        return socialConnectionRepository.findByProviderTypeAndProviderId(providerType, providerUserId)
                                         .map(SocialConnection::getUser);
    }

    /**
     * 소셜 인증 정보를 사용하여 회원 정보 생성
     *
     * @param dto 신규 회원 가입 요청 DTO
     * @return 회원 엔티티
     */
    @Override
    @Transactional
    public User createUserForSocialConnection(final OAuth2SignupRequest dto) {
        final ProviderType providerType = ProviderType.from(dto.getProvider());
        final MapApi       mapApi       = MapApi.from(dto.getMapApi());

        return switch (providerType) {
            case APPLE -> {
                AppleTokenValidationResponse response =
                        idpTokenExchangeClient.validationAppleAuthCode(dto.getAuthCode());
                yield createNewUser(providerType, response.idToken(), response.refreshToken(),
                                    dto.getEmail(), dto.getNickname(), mapApi);
            }
            case GOOGLE -> {
                GoogleTokenValidationResponse response =
                        idpTokenExchangeClient.validationGoogleAuthCode(dto.getAuthCode());
                yield createNewUser(providerType, response.idToken(), response.refreshToken(),
                                    dto.getEmail(), dto.getNickname(), mapApi);
            }
            default -> throw new GlobalException(SOCIAL_CONNECTION_PROVIDER_NOT_SUPPORTED);
        };
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
                                         .orElseThrow(() -> new GlobalException(SOCIAL_CONNECTION_NOT_FOUND));
    }

    /**
     * ID로 회원 정보 조회
     *
     * @param id 회원 ID
     * @return 회원 정보 응답 DTO
     */
    @Override
    public UserInfoResponse findUserInfoById(final UUID id) {
        return userRepository.findUserInfoById(id).orElseThrow(() -> new GlobalException(USER_NOT_FOUND));
    }

    /**
     * 회원 정보 수정
     *
     * @param id  회원 ID
     * @param dto 회원 정보 수정 요청 DTO
     */
    @Override
    @Transactional
    public void updateUserInfo(final UUID id, final UserInfoUpdateRequest dto) {
        final MapApi mapApi = MapApi.from(dto.getMapApi());

        User user = userRepository.findById(id).orElseThrow(() -> new GlobalException(USER_NOT_FOUND));

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
        User user = userRepository.findById(id).orElseThrow(() -> new GlobalException(USER_NOT_FOUND));
        SocialConnection socialConnection = socialConnectionRepository.findByUserId(id)
                                                                      .orElseThrow(() -> new GlobalException(SOCIAL_CONNECTION_NOT_FOUND));

        revokeIdpToken(socialConnection);
        socialConnectionRepository.delete(socialConnection);
        userRepository.delete(user);
    }

    // ========================= 내부 메서드 =========================

    /**
     * IDP 토큰 폐기 (전략 패턴)
     *
     * @param socialConnection 소셜 연결 정보
     */
    private void revokeIdpToken(final SocialConnection socialConnection) {
        Function<String, Void> revokeAction = switch (socialConnection.getProviderType()) {
            case APPLE -> token -> {
                idpTokenExchangeClient.revokeAppleToken(REFRESH_TOKEN, token);
                return null;
            };
            case GOOGLE -> token -> {
                idpTokenExchangeClient.revokeGoogleToken(token);
                return null;
            };
            default -> throw new GlobalException(SOCIAL_CONNECTION_PROVIDER_NOT_SUPPORTED);
        };

        revokeAction.apply(socialConnection.getIdpRefreshToken());
    }

    /**
     * 신규 회원 엔티티 생성
     *
     * @param providerType 소셜 인증 제공자 유형
     * @param idToken      소셜 ID 토큰
     * @param refreshToken 소셜 Refresh Token
     * @param email        이메일
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
     * 이메일 중복 검증
     *
     * @param email 이메일
     * @throws GlobalException USER_EMAIL_REQUIRED 또는 USER_EMAIL_DUPLICATED
     */
    private void validateEmail(final String email) {
        if (email == null || email.isBlank()) throw new GlobalException(USER_EMAIL_REQUIRED);
        if (userRepository.existsByEmail(email)) throw new GlobalException(USER_EMAIL_DUPLICATED);
    }

}
