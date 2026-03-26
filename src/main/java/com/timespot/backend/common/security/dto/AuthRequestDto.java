package com.timespot.backend.common.security.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static lombok.AccessLevel.PRIVATE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.common.security.dto
 * FileName    : AuthRequestDto
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description : 인증 관련 요청 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 * 26. 3. 24.    loadingKKamo21       API 문서 상세화 (예시 값 추가)
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(description = "OAuth2 소셜 로그인 요청 페이로드")
public abstract class AuthRequestDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "소셜 회원가입 요청 페이로드")
    public static class OAuth2SignupRequest {

        @NotBlank(message = "소셜 인증 제공자는 필수입니다.")
        @Schema(
                description = """
                              [필수] 소셜 인증 제공자
                              
                              - `apple`: Apple OAuth2
                              - `google`: Google OAuth2
                              """,
                example = "apple",
                requiredMode = REQUIRED
        )
        private String provider;

        @NotBlank(message = "인증 코드는 필수입니다.")
        @Schema(
                description = """
                              [필수] 소셜 인증 제공자로부터 발급받은 인증 코드
                              - Apple: authorization code from ASAuthorizationController
                              - Google: authorization code from Google Sign-In
                              """,
                example = "eyJhbGciOiJIUzUxMiJ9...",
                requiredMode = REQUIRED
        )
        private String authCode;

        @NotBlank(message = "이메일은 필수입니다.")
        @Schema(
                description = "[필수] 사용자 이메일 주소",
                example = "user@example.com",
                requiredMode = REQUIRED
        )
        private String email;

        @NotBlank(message = "닉네임은 필수입니다.")
        @Schema(
                description = """
                              [필수] 닉네임
                              
                              - 2~15 자
                              - 한글, 영문, 숫자, '-', '_'만 사용 가능
                              """,
                example = "홍길동",
                requiredMode = REQUIRED
        )
        private String nickname;

        @NotBlank(message = "주사용 지도 API 유형은 필수입니다.")
        @Schema(
                description = """
                              [필수] 주사용 지도 API 유형
                              
                              - `apple`: 애플 지도
                              - `google`: 구글 지도
                              - `naver`: 네이버 지도
                              """,
                example = "apple",
                requiredMode = REQUIRED
        )
        private String mapApi;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "소셜 로그인 토큰 요청 페이로드")
    public static class OAuth2LoginRequest {

        @NotBlank(message = "소셜 인증 제공자는 필수입니다.")
        @Schema(
                description = """
                              [필수] 소셜 인증 제공자
                              
                              - `apple`: Apple OAuth2
                              - `google`: Google OAuth2
                              """,
                example = "apple",
                requiredMode = REQUIRED
        )
        private String provider;

        @NotBlank(message = "소셜 인증 ID 토큰은 필수입니다.")
        @Schema(
                description = """
                              [필수] 소셜 인증 제공자로부터 발급받은 ID Token
                              - Apple: identityToken from ASAuthorizationAppleIDCredential
                              - Google: idToken from GoogleSignInAccount
                              """,
                example = "eyJhbGciOiJIUzUxMiJ9...",
                requiredMode = REQUIRED
        )
        private String idToken;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "토큰 갱신 요청 페이로드")
    public static class TokenRefreshRequest {

        @NotBlank(message = "리프레시 토큰은 필수입니다.")
        @Schema(
                description = """
                              [필수] JWT Refresh Token
                              
                              - 로그인 또는 토큰 갱신 시 발급받은 14 일 유효 토큰
                              - Bearer 접두사 없이 토큰 값만 전송
                              """,
                example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhMWIyYzNkNC1lNWY2LTc4OTAtYWJjZC1lZjEyMzQ1Njc4OTAiLCJ1c2VybmFtZSI6InVzZXJAZXhhbXBsZS5jb20iLCJwcm92aWRlciI6IkFQUExFIiwibWFwX2FwaSI6IkFQUExFIiwiYXV0aG9yaXRpZXMiOiJST0xFX1VTRVIiLCJpYXQiOjE3MDk1NTYwMDAsImV4cCI6MTcxMDc2NTYwMH0...",
                requiredMode = REQUIRED
        )
        private String refreshToken;

    }

}
