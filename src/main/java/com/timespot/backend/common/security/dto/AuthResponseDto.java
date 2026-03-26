package com.timespot.backend.common.security.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.timespot.backend.domain.user.model.MapApi;
import com.timespot.backend.domain.user.model.ProviderType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.timespot.backend.common.security.dto
 * FileName    : AuthResponseDto
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description : 인증 관련 응답 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 * 26. 3. 24.    loadingKKamo21       API 문서 상세화 (예시 값 추가)
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(description = "OAuth2 소셜 로그인 응답 페이로드")
public abstract class AuthResponseDto {

    @Getter
    @RequiredArgsConstructor
    @JsonInclude(NON_NULL)
    @Schema(name = "인증 토큰 정보 응답 페이로드", description = "JWT 토큰 및 만료 시간 정보")
    public static class TokenInfoResponse {

        @Schema(
                description = "JWT Access Token",
                example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhMWIyYzNkNC1lNWY2LTc4OTAtYWJjZC1lZjEyMzQ1Njc4OTAiLCJ1c2VybmFtZSI6InVzZXJAZXhhbXBsZS5jb20iLCJwcm92aWRlciI6IkFQUExFIiwibWFwX2FwaSI6IkFQUExFIiwiYXV0aG9yaXRpZXMiOiJST0xFX1VTRVIiLCJpYXQiOjE3MDk1NTYwMDAsImV4cCI6MTcxMDc2NTYwMH0.abc123..."
        )
        private final String accessToken;

        @Schema(
                description = "Access Token 만료 시간 (초 단위)",
                example = "604800",
                accessMode = READ_ONLY
        )
        private final Long accessTokenExpiresIn;

        @Schema(
                description = "JWT Refresh Token",
                example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhMWIyYzNkNC1lNWY2LTc4OTAtYWJjZC1lZjEyMzQ1Njc4OTAiLCJ1c2VybmFtZSI6InVzZXJAZXhhbXBsZS5jb20iLCJwcm92aWRlciI6IkFQUExFIiwibWFwX2FwaSI6IkFQUExFIiwiYXV0aG9yaXRpZXMiOiJST0xFX1VTRVIiLCJpYXQiOjE3MDk1NTYwMDAsImV4cCI6MTcxMTk2ODAwMH0.def456..."
        )
        private final String refreshToken;

        @Schema(
                description = "Refresh Token 만료 시간 (초 단위)",
                example = "1209600",
                accessMode = READ_ONLY
        )
        private final Long refreshTokenExpiresIn;

    }

    @Getter
    @JsonInclude(NON_NULL)
    @Schema(name = "인증 정보 응답 페이로드", description = "사용자 인증 정보 및 토큰 정보")
    public static class AuthInfoResponse extends TokenInfoResponse {

        @Schema(
                description = "회원의 주사용 지도 API 정보 (신규 회원가입/로그인 시)",
                nullable = true,
                accessMode = READ_ONLY
        )
        private Map map;

        @Schema(
                description = "소셜 인증 제공자 유형 (APPLE, GOOGLE)",
                example = "APPLE",
                nullable = true,
                accessMode = READ_ONLY
        )
        private String socialType;

        @Schema(
                description = "신규 회원 여부 (true: 회원가입 필요, false: 기존 사용자)",
                example = "false",
                nullable = true,
                accessMode = READ_ONLY
        )
        private Boolean newUser;

        @Schema(
                description = "사용자 기본 정보 (이메일, 닉네임)",
                nullable = true,
                accessMode = READ_ONLY
        )
        private UserInfo userInfo;

        public AuthInfoResponse(final String accessToken,
                                final Long accessTokenExpiresIn,
                                final String refreshToken,
                                final Long refreshTokenExpiresIn,
                                final MapApi mapApi,
                                final ProviderType providerType,
                                final Boolean newUser,
                                final String email,
                                final String nickname) {
            super(accessToken, accessTokenExpiresIn, refreshToken, refreshTokenExpiresIn);
            this.map = mapApi != null ? new Map(mapApi.getName(), mapApi.getUrlScheme()) : null;
            this.newUser = newUser != null && newUser;
            this.socialType = providerType.name();
            this.userInfo = new UserInfo(email, nickname);
        }

        public AuthInfoResponse(final ProviderType providerType,
                                final Boolean newUser,
                                final String email,
                                final String nickname) {
            super(null, null, null, null);
            this.map = null;
            this.socialType = providerType.name();
            this.newUser = newUser != null && newUser;
            this.userInfo = new UserInfo(email, nickname);
        }

        @Getter
        @RequiredArgsConstructor
        @JsonInclude(NON_NULL)
        @Schema(name = "지도 API 정보", description = "현재 사용자의 주사용 지도 API 관련 정보")
        static class Map {

            @Schema(
                    description = "지도 API 이름",
                    example = "구글",
                    accessMode = READ_ONLY
            )
            private final String mapName;

            @Schema(
                    description = "지도 앱 URL 스키마 (네비게이션 링크 생성 시 사용)",
                    example = "comgooglemaps",
                    nullable = true,
                    accessMode = READ_ONLY
            )
            private final String mapUrlScheme;

        }

        @Getter
        @RequiredArgsConstructor
        @JsonInclude(NON_NULL)
        @Schema(name = "사용자 정보", description = "소셜 ID 토큰 내 사용자 정보")
        static class UserInfo {

            @Schema(
                    description = "사용자 이메일 주소",
                    example = "user@example.com",
                    accessMode = READ_ONLY
            )
            private final String email;

            @Schema(
                    description = "사용자 닉네임",
                    example = "홍길동",
                    accessMode = READ_ONLY
            )
            private final String nickname;

        }

    }

}
