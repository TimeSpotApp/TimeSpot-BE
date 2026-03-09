package com.nomadspot.backend.common.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.nomadspot.backend.common.security.dto
 * FileName    : AuthResponseDto
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "OAuth2 소셜 로그인 응답 페이로드")
public abstract class AuthResponseDto {

    @Getter
    @RequiredArgsConstructor
    @Schema(name = "인증 토큰 응답 페이로드")
    public static class TokenResponse {
        @Schema(description = "Access Token")
        private final String accessToken;
        @Schema(description = "Access Token 만료 시간(초)")
        private final long   accessTokenExpiresIn;
        @Schema(description = "Refresh Token")
        private final String refreshToken;
        @Schema(description = "Refresh Token 만료 시간(초)")
        private final long   refreshTokenExpiresIn;
    }

}
