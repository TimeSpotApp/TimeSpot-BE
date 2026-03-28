package com.timespot.backend.domain.user.api;

import static com.timespot.backend.common.response.SuccessCode.USER_GET_INFO_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.USER_NOTIFICATION_SETTINGS_GET_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.USER_NOTIFICATION_SETTINGS_UPDATE_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.USER_UPDATE_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.USER_WITHDRAW_SUCCESS;
import static com.timespot.backend.domain.user.constant.NotificationType.DEPARTURE_10_MIN_BEFORE;
import static com.timespot.backend.domain.user.constant.NotificationType.DEPARTURE_15_MIN_BEFORE;
import static com.timespot.backend.domain.user.constant.NotificationType.DEPARTURE_5_MIN_BEFORE;
import static com.timespot.backend.domain.user.constant.NotificationType.DEPARTURE_TIME;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.SuccessCode;
import com.timespot.backend.common.security.dto.AuthResponseDto.AuthInfoResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.user.dto.UserRequestDto;
import com.timespot.backend.domain.user.constant.NotificationType;
import com.timespot.backend.domain.user.dto.UserNotificationRequestDto.NotificationSettingsRequest;
import com.timespot.backend.domain.user.dto.UserNotificationResponseDto.NotificationSettingsResponse;
import com.timespot.backend.domain.user.dto.UserRequestDto.UserInfoUpdateRequest;
import com.timespot.backend.domain.user.dto.UserResponseDto;
import com.timespot.backend.domain.user.dto.UserResponseDto.UserInfoResponse;
import com.timespot.backend.domain.user.facade.UserAuthFacade;
import com.timespot.backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.timespot.backend.domain.user.api
 * FileName    : UserController
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description : 사용자 API 컨트롤러 (회원 정보 조회, 수정, 탈퇴)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserApiDocs {

    private final UserService    userService;
    private final UserAuthFacade userAuthFacade;

    @GetMapping
    @Override
    public ResponseEntity<BaseResponse<UserInfoResponse>> getUserInfo(
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        UserInfoResponse responseData = userService.findUserInfoById(userDetails.getId());
        return ResponseEntity.ok(BaseResponse.success(USER_GET_INFO_SUCCESS, responseData));
    }

    @PostMapping
    @Override
    public ResponseEntity<BaseResponse<AuthInfoResponse>> updateUserInfo(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @RequestBody final UserInfoUpdateRequest dto
    ) {
        AuthInfoResponse responseData = userAuthFacade.updateUserInfoAndReissueToken(userDetails.getId(), dto);
        return ResponseEntity.ok(BaseResponse.success(USER_UPDATE_SUCCESS, responseData));
    }

    @GetMapping("/notifications")
    @Override
    public ResponseEntity<BaseResponse<UserResponseDto.UserNotificationResponse>> getUserNotificationSettings(
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        UserResponseDto.UserNotificationResponse responseData = userService.findUserNotificationSettings(userDetails.getId());
        return ResponseEntity.ok(BaseResponse.success(SuccessCode.USER_NOTIFICATION_GET_SUCCESS, responseData));
    }

    @PatchMapping("/notifications")
    @Override
    public ResponseEntity<BaseResponse<Void>> updateUserNotificationSettings(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @RequestBody @Valid final UserRequestDto.UserNotificationUpdateRequest dto
    ) {
        userService.updateUserNotificationSettings(userDetails.getId(), dto);
        return ResponseEntity.ok(BaseResponse.success(SuccessCode.USER_NOTIFICATION_UPDATE_SUCCESS));
    }


    @DeleteMapping
    @Override
    public ResponseEntity<BaseResponse<Void>> withdraw(
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        userService.withdraw(userDetails.getId());
        return ResponseEntity.ok(BaseResponse.success(USER_WITHDRAW_SUCCESS));
    }

    @GetMapping("/notification-settings")
    @Override
    public ResponseEntity<BaseResponse<NotificationSettingsResponse>> getNotificationSettings(
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        // TODO: 서비스 계층 개발 구현
        // NotificationSettingsResponse responseData = userService.getNotificationSettings(userDetails.getId());

        // 임시 응답 (서비스 계층 개발 시 제거)
        NotificationSettingsResponse responseData = new NotificationSettingsResponse(
                java.util.List.of(
                        new com.timespot.backend.domain.user.dto.UserNotificationResponseDto.NotificationSettingItem(
                                DEPARTURE_TIME, true, false
                        ),
                        new com.timespot.backend.domain.user.dto.UserNotificationResponseDto.NotificationSettingItem(
                                DEPARTURE_5_MIN_BEFORE, true, true
                        ),
                        new com.timespot.backend.domain.user.dto.UserNotificationResponseDto.NotificationSettingItem(
                                DEPARTURE_10_MIN_BEFORE, true, true
                        ),
                        new com.timespot.backend.domain.user.dto.UserNotificationResponseDto.NotificationSettingItem(
                                DEPARTURE_15_MIN_BEFORE, false, true
                        )
                ),
                java.time.LocalDateTime.now()
        );

        return ResponseEntity.ok(BaseResponse.success(USER_NOTIFICATION_SETTINGS_GET_SUCCESS, responseData));
    }

    @PutMapping("/notification-settings")
    @Override
    public ResponseEntity<BaseResponse<NotificationSettingsResponse>> updateNotificationSettings(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @RequestBody final NotificationSettingsRequest dto
    ) {
        // TODO: 서비스 계층 개발 구현
        // NotificationSettingsResponse responseData = userService.updateNotificationSettings(userDetails.getId(), dto);

        // 임시 응답 (서비스 계층 개발 시 제거)
        NotificationSettingsResponse responseData = new NotificationSettingsResponse(
                java.util.List.of(
                        new com.timespot.backend.domain.user.dto.UserNotificationResponseDto.NotificationSettingItem(
                                DEPARTURE_TIME, true, false
                        ),
                        createNotificationSettingItem(
                                dto,
                                DEPARTURE_5_MIN_BEFORE
                        ),
                        createNotificationSettingItem(
                                dto,
                                DEPARTURE_10_MIN_BEFORE
                        ),
                        createNotificationSettingItem(
                                dto,
                                DEPARTURE_15_MIN_BEFORE
                        )
                ),
                java.time.LocalDateTime.now()
        );

        return ResponseEntity.ok(BaseResponse.success(USER_NOTIFICATION_SETTINGS_UPDATE_SUCCESS, responseData));
    }

    // ========================= 내부 메서드 =========================

    /**
     * DTO 에서 특정 알림 타입의 설정 항목을 생성
     *
     * @param dto        요청 DTO
     * @param targetType 대상 알림 타입
     * @return 응답 설정 항목
     */
    private com.timespot.backend.domain.user.dto.UserNotificationResponseDto.NotificationSettingItem createNotificationSettingItem(
            final NotificationSettingsRequest dto,
            final NotificationType targetType
    ) {
        return dto.getNotificationSettings()
                  .stream()
                  .filter(item -> item.getType() == targetType)
                  .findFirst()
                  .map(item -> new com.timespot.backend.domain.user.dto.UserNotificationResponseDto.NotificationSettingItem(
                          item.getType(),
                          item.getIsEnabled(),
                          true
                  ))
                  .orElse(new com.timespot.backend.domain.user.dto.UserNotificationResponseDto.NotificationSettingItem(
                          targetType,
                          false,
                          true
                  ));
    }

}
