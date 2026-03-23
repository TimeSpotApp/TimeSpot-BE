package com.timespot.backend.common.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.common.security.dto
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

        @NotBlank(message = "소셜 인증 제공자는 필수입니다.")
        @Schema(description = "[필수] 소셜 인증 제공자 (예: apple, google)", example = "apple")
        private String provider;

        @NotBlank(message = "소셜 인증 ID 토큰은 필수입니다.")
        @Schema(description = "[필수] 소셜 인증 ID 토큰")
        private String idToken;

        @Schema(description = "[선택/최초 로그인(회원가입) 시 필수] 1회용 인가 코드", nullable = true)
        private String authCode;

        @Schema(description = "[선택] 사용자 이름/닉네임 (APPLE 최초 로그인 시 필수)", nullable = true)
        private String nickname;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "토큰 갱신 요청 페이로드")
    public static class TokenRefreshRequest {

        @NotBlank(message = "리프레시 토큰은 필수입니다.")
        @Schema(description = "[필수] JWT Refresh Token")
        private String refreshToken;

    }

}
