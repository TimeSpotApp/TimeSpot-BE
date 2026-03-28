package com.timespot.backend.domain.user.service;

import com.timespot.backend.domain.user.dto.UserNotificationRequestDto.NotificationSettingsRequest;
import com.timespot.backend.domain.user.dto.UserNotificationResponseDto.NotificationSettingsResponse;
import com.timespot.backend.common.security.dto.AuthRequestDto.OAuth2SignupRequest;
import com.timespot.backend.domain.user.dto.UserRequestDto.UserInfoUpdateRequest;
import com.timespot.backend.domain.user.dto.UserResponseDto.UserInfoResponse;
import com.timespot.backend.domain.user.model.ProviderType;
import com.timespot.backend.domain.user.model.SocialConnection;
import com.timespot.backend.domain.user.model.User;
import java.util.Optional;
import java.util.UUID;

/**
 * PackageName : com.timespot.backend.domain.user.service
 * FileName    : UserService
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description : 사용자 도메인 서비스 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 * 26. 3. 24.    loadingKKamo21       JavaDoc 개선
 */
public interface UserService {

    /**
     * 소셜 인증 정보를 사용하여 회원 정보 조회
     *
     * @param providerType   소셜 인증 제공자 유형
     * @param providerUserId 소셜 인증 제공자 식별자
     * @return 회원 엔티티 (Optional)
     */
    Optional<User> findUserForSocialConnection(ProviderType providerType, String providerUserId);

    /**
     * 소셜 인증 정보를 사용하여 신규 회원 생성
     *
     * @param dto 신규 회원 가입 요청 DTO
     * @return 생성된 회원 엔티티
     */
    User createUserForSocialConnection(OAuth2SignupRequest dto);

    /**
     * 회원 ID 로 소셜 연결 정보 조회
     *
     * @param userId 회원 ID
     * @return 소셜 연결 엔티티
     * @throws com.timespot.backend.common.error.GlobalException (SOCIAL_CONNECTION_NOT_FOUND)
     */
    SocialConnection findByUserId(UUID userId);

    /**
     * 회원 ID 로 회원 정보 조회
     *
     * @param id 회원 ID
     * @return 회원 정보 응답 DTO
     * @throws com.timespot.backend.common.error.GlobalException (USER_NOT_FOUND)
     */
    UserInfoResponse findUserInfoById(UUID id);

    /**
     * 회원 정보 수정
     *
     * @param id  회원 ID
     * @param dto 회원 정보 수정 요청 DTO
     * @throws com.timespot.backend.common.error.GlobalException (USER_NOT_FOUND)
     */
    void updateUserInfo(UUID id, UserInfoUpdateRequest dto);

    NotificationSettingsResponse getNotificationSettings(UUID id);


    NotificationSettingsResponse updateNotificationSettings(UUID id, NotificationSettingsRequest dto);

    /**
     * 회원 탈퇴 처리
     * <p>
     * 수행 작업:
     * 1. IDP(Apple/Google) 리프레시 토큰 폐기
     * 2. 소셜 연결 정보 삭제
     * 3. 사용자 정보 삭제
     * </p>
     *
     * @param id 회원 ID
     * @throws com.timespot.backend.common.error.GlobalException (USER_NOT_FOUND, SOCIAL_CONNECTION_NOT_FOUND)
     */
    void withdraw(UUID id);

}
