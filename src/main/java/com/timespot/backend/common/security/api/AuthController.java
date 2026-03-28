package com.timespot.backend.common.security.api;

import static com.timespot.backend.common.response.SuccessCode.DEVICE_REGISTER_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.USER_AUTH_LOGIN_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.USER_AUTH_LOGOUT_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.USER_AUTH_TOKEN_REFRESH_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.USER_REGISTER_SUCCESS;
import static com.timespot.backend.common.security.constant.SecurityConst.JWT_ACCESS_TOKEN_HEADER;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.security.dto.AuthRequestDto.OAuth2LoginRequest;
import com.timespot.backend.common.security.dto.AuthRequestDto.OAuth2SignupRequest;
import com.timespot.backend.common.security.dto.AuthRequestDto.TokenRefreshRequest;
import com.timespot.backend.common.security.dto.AuthResponseDto.AuthInfoResponse;
import com.timespot.backend.common.security.dto.AuthResponseDto.TokenInfoResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.common.security.service.AuthService;
import com.timespot.backend.domain.device.dto.DeviceRequestDto.DeviceRegisterRequest;
import com.timespot.backend.domain.device.dto.DeviceResponseDto.DeviceRegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.timespot.backend.common.security.api
 * FileName    : AuthController
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description : 인증/인가 API 컨트롤러 (로그인, 회원가입, 토큰 관리, 디바이스 등록)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 * 26. 3. 28.    loadingKKamo21       디바이스 등록 API 추가 (/api/v1/auth/devices)
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApiDocs {

    private final AuthService authService;

    @Override
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<AuthInfoResponse>> signup(
            @RequestBody final OAuth2SignupRequest dto
    ) {
        AuthInfoResponse responseData = authService.signup(dto);
        return ResponseEntity.status(CREATED).body(BaseResponse.success(USER_REGISTER_SUCCESS, responseData));
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<AuthInfoResponse>> login(
            @RequestBody final OAuth2LoginRequest dto
    ) {
        AuthInfoResponse responseData = authService.login(dto);
        final boolean    isNewUser    = responseData.getNewUser() != null && responseData.getNewUser();
        final HttpStatus status       = isNewUser ? ACCEPTED : OK;
        return ResponseEntity.status(status).body(
                isNewUser ? BaseResponse.of(status, "로그인 요청 처리가 완료되었습니다.", responseData)
                          : BaseResponse.success(USER_AUTH_LOGIN_SUCCESS, responseData)
        );
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(
            @RequestHeader(JWT_ACCESS_TOKEN_HEADER) final String authorizationHeader
    ) {
        String accessToken = authorizationHeader.substring(7);
        authService.logout(accessToken);
        return ResponseEntity.ok(BaseResponse.success(USER_AUTH_LOGOUT_SUCCESS));
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<TokenInfoResponse>> refresh(
            @RequestBody final TokenRefreshRequest dto
    ) {
        TokenInfoResponse responseData = authService.refresh(dto.getRefreshToken());
        return ResponseEntity.ok(BaseResponse.success(USER_AUTH_TOKEN_REFRESH_SUCCESS, responseData));
    }

    @Override
    @PostMapping("/devices")
    public ResponseEntity<BaseResponse<DeviceRegisterResponse>> registerDevice(
            @RequestBody final DeviceRegisterRequest dto,
            @org.springframework.security.core.annotation.AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        // TODO: 서비스 계층 개발 시 구현
        // DeviceRegisterResponse responseData = deviceService.registerDevice(dto, userDetails);

        // 임시 응답 (서비스 계층 개발 시 제거)
        String userId = (userDetails != null) ? userDetails.getId().toString() : null;
        DeviceRegisterResponse responseData = new DeviceRegisterResponse(
                userId,
                dto.getDeviceToken(),
                true
        );

        return ResponseEntity.ok(BaseResponse.success(DEVICE_REGISTER_SUCCESS, responseData));
    }

}
