package com.timespot.backend.domain.user.api;

import static com.timespot.backend.common.response.SuccessCode.USER_GET_INFO_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.USER_UPDATE_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.USER_WITHDRAW_SUCCESS;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.security.dto.AuthResponseDto.AuthInfoResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.user.dto.UserRequestDto.UserInfoUpdateRequest;
import com.timespot.backend.domain.user.dto.UserResponseDto.UserInfoResponse;
import com.timespot.backend.domain.user.facade.UserAuthFacade;
import com.timespot.backend.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
            @RequestBody @Valid final UserInfoUpdateRequest dto
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

}
