package com.nomadspot.backend.common.security.api;

import com.nomadspot.backend.common.response.ApiResponse;
import com.nomadspot.backend.common.response.SuccessCode;
import com.nomadspot.backend.common.security.constant.SecurityConst;
import com.nomadspot.backend.common.security.dto.AuthRequestDto;
import com.nomadspot.backend.common.security.dto.AuthResponseDto;
import com.nomadspot.backend.common.security.dto.AuthResponseDto.TokenResponse;
import com.nomadspot.backend.common.security.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "인증 관련 API", description = "OAuth 인증을 활용한 로그인, 로그아웃, 토큰 갱신을 수행합니다.")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login/{provider}")
    @Operation(summary = "소셜 로그인", description = "소셜 인증 정보를 활용하여 로그인합니다.")
    public ResponseEntity<ApiResponse<AuthResponseDto.TokenResponse>> login(
            @PathVariable("provider") final String provider,
            @RequestBody final AuthRequestDto.OAuth2LoginRequest dto
    ) {
        TokenResponse responseData = authService.login(provider, dto.getProviderToken());
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.USER_AUTH_LOGIN_SUCCESS, responseData));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그인된 상태에서 로그아웃합니다.")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(SecurityConst.JWT_ACCESS_TOKEN_HEADER) final String authorizationHeader
    ) {
        String accessToken = authorizationHeader.substring(7);
        authService.logout(accessToken);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.USER_AUTH_LOGOUT_SUCCESS));
    }

    @PostMapping("/refresh")
    @Operation(summary = "JWT 갱신", description = "RefreshToken을 사용하여 JWT를 갱신합니다.")
    public ResponseEntity<ApiResponse<AuthResponseDto.TokenResponse>> refresh(
            @RequestBody final AuthRequestDto.TokenRefreshRequest dto
    ) {
        TokenResponse responseData = authService.refresh(dto.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.USER_AUTH_TOKEN_REFRESH_SUCCESS, responseData));
    }

}
