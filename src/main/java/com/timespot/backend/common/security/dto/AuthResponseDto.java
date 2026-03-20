package com.timespot.backend.common.security.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.timespot.backend.common.security.dto
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
    @AllArgsConstructor
    @Schema(name = "인증 토큰 응답 페이로드")
    public static class TokenResponse {

        @Schema(description = "Access Token")
        private final String  accessToken;
        @Schema(description = "Access Token 만료 시간(초)")
        private final long    accessTokenExpiresIn;
        @Schema(description = "Refresh Token")
        private final String  refreshToken;
        @Schema(description = "Refresh Token 만료 시간(초)")
        private final long    refreshTokenExpiresIn;
        @JsonInclude(Include.NON_NULL)
        @Schema(description = "신규 회원 여부", nullable = true)
        private       Boolean newUser = null;

    }

}
