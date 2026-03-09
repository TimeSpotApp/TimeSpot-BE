package com.nomadspot.backend.common.security.api;

import com.nomadspot.backend.common.response.BaseResponse;
import com.nomadspot.backend.common.response.SuccessCode;
import com.nomadspot.backend.common.security.constant.SecurityConst;
import com.nomadspot.backend.common.security.dto.AuthRequestDto;
import com.nomadspot.backend.common.security.dto.AuthResponseDto.TokenResponse;
import com.nomadspot.backend.common.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.nomadspot.backend.common.security.api
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
    @PostMapping("/login/{provider}")
    public ResponseEntity<BaseResponse<TokenResponse>> login(
            @PathVariable("provider") final String provider,
            @RequestBody final AuthRequestDto.OAuth2LoginRequest dto
    ) {
        TokenResponse responseData = authService.login(provider, dto.getProviderToken());
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
            @RequestBody final AuthRequestDto.TokenRefreshRequest dto
    ) {
        TokenResponse responseData = authService.refresh(dto.getRefreshToken());
        return ResponseEntity.ok(BaseResponse.success(SuccessCode.USER_AUTH_TOKEN_REFRESH_SUCCESS, responseData));
    }

}
