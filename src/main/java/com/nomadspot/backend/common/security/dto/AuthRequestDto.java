package com.nomadspot.backend.common.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.nomadspot.backend.common.security.dto
 * FileName    : AuthRequestDto
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "OAuth2 소셜 로그인 요청 페이로드")
public abstract class AuthRequestDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "소셜 로그인 토큰 요청 페이로드")
    public static class OAuth2LoginRequest {

        @Schema(description = "소셜 인증 토큰")
        private String providerToken;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "토큰 갱신 요청 페이로드")
    public static class TokenRefreshRequest {

        @Schema(description = "JWT Refresh Token")
        private String refreshToken;

    }

}
