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
@Schema(name = "인증 응답 DTO")
public abstract class AuthResponseDto {

    @Getter
    @RequiredArgsConstructor
    @Schema(name = "토큰 응답 DTO")
    public static class TokenResponse {
        private final String accessToken;
        private final long   accessTokenExpiresIn;
        private final String refreshToken;
        private final long   refreshTokenExpiresIn;
    }

}
