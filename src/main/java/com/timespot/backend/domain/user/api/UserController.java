package com.timespot.backend.domain.user.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.SuccessCode;
import com.timespot.backend.common.security.dto.AuthResponseDto;
import com.timespot.backend.common.security.dto.AuthResponseDto.AuthInfoResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.user.dto.UserRequestDto;
import com.timespot.backend.domain.user.dto.UserResponseDto;
import com.timespot.backend.domain.user.facade.UserAuthFacade;
import com.timespot.backend.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
 * Description :
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
    public ResponseEntity<BaseResponse<UserResponseDto.UserInfoResponse>> getUserInfo(
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        UserResponseDto.UserInfoResponse responseData = userService.findUserInfoById(userDetails.getId());
        return ResponseEntity.ok(BaseResponse.success(SuccessCode.USER_GET_INFO_SUCCESS, responseData));
    }

    @PutMapping
    @Override
    public ResponseEntity<BaseResponse<AuthResponseDto.AuthInfoResponse>> updateUserInfo(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @RequestBody @Valid final UserRequestDto.UserInfoUpdateRequest dto
    ) {
        AuthInfoResponse responseData = userAuthFacade.updateUserInfoAndReissueToken(userDetails.getId(), dto);
        return ResponseEntity.ok(BaseResponse.success(SuccessCode.USER_UPDATE_SUCCESS, responseData));
    }

    @PostMapping("/map")
    @Override
    public ResponseEntity<BaseResponse<AuthResponseDto.AuthInfoResponse>> updateUserMapApi(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @RequestBody @Valid final UserRequestDto.UserMapApiUpdateRequest dto
    ) {
        AuthInfoResponse responseData = userAuthFacade.updateUserMapApiAndReissueToken(userDetails.getId(), dto);
        return ResponseEntity.ok(BaseResponse.success(SuccessCode.USER_MAP_API_UPDATE_SUCCESS, responseData));
    }

    @DeleteMapping
    @Override
    public ResponseEntity<BaseResponse<Void>> withdraw(
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        userService.withdraw(userDetails.getId());
        return ResponseEntity.ok(BaseResponse.success(SuccessCode.USER_WITHDRAW_SUCCESS));
    }

}
