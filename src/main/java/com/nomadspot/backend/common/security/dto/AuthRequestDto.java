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
@Schema(name = "인증 요청 DTO")
public abstract class AuthRequestDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "OAuth2 로그인 요청 DTO")
    public static class OAuth2LoginRequest {
        private String providerToken;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "토큰 갱신 요청 DTO")
    public static class TokenRefreshRequest {
        private String refreshToken;
    }

}
