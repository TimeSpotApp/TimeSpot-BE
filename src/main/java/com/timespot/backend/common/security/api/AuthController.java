package com.timespot.backend.common.security.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.SuccessCode;
import com.timespot.backend.common.security.constant.SecurityConst;
import com.timespot.backend.common.security.dto.AuthRequestDto;
import com.timespot.backend.common.security.dto.AuthResponseDto;
import com.timespot.backend.common.security.dto.AuthResponseDto.TokenResponse;
import com.timespot.backend.common.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApiDocs {

    private final AuthService authService;

    @Override
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<TokenResponse>> login(
            @RequestBody @Valid final AuthRequestDto.OAuth2LoginRequest dto
    ) {
        AuthResponseDto.TokenResponse responseData = authService.login(dto);
        return ResponseEntity.ok(BaseResponse.success(SuccessCode.USER_AUTH_LOGIN_SUCCESS, responseData));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(
            @RequestHeader(SecurityConst.JWT_ACCESS_TOKEN_HEADER) final String authorizationHeader
    ) {
        String accessToken = authorizationHeader.substring(7);
        authService.logout(accessToken);

        return ResponseEntity.ok(BaseResponse.success(SuccessCode.USER_AUTH_LOGOUT_SUCCESS));
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<TokenResponse>> refresh(
            @RequestBody @Valid final AuthRequestDto.TokenRefreshRequest dto
    ) {
        AuthResponseDto.TokenResponse responseData = authService.refresh(dto.getRefreshToken());
        return ResponseEntity.ok(BaseResponse.success(SuccessCode.USER_AUTH_TOKEN_REFRESH_SUCCESS, responseData));
    }

}
