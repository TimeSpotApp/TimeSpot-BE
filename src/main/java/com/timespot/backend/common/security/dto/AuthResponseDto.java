package com.timespot.backend.common.security.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.timespot.backend.domain.user.model.MapApi;
import com.timespot.backend.domain.user.model.ProviderType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
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
    @JsonInclude(Include.NON_NULL)
    @Schema(name = "인증 정보 응답 페이로드")
    public static class AuthInfoResponse {

        @Schema(description = "Access Token")
        private final String   accessToken;
        @Schema(description = "Access Token 만료 시간(초)")
        private final Long     accessTokenExpiresIn;
        @Schema(description = "Refresh Token")
        private final String   refreshToken;
        @Schema(description = "Refresh Token 만료 시간(초)")
        private final Long     refreshTokenExpiresIn;
        @Schema(description = "회원의 주사용 지도 API 정보", nullable = true)
        private       Map      map        = null;
        @Schema(description = "소셜 인증 제공자", nullable = true)
        private       String   socialType = null;
        @Schema(description = "신규 회원 여부", nullable = true)
        private       Boolean  newUser    = null;
        @Schema(description = "사용자 정보", nullable = true)
        private       UserInfo userInfo   = null;

        public AuthInfoResponse(final String accessToken,
                                final Long accessTokenExpiresIn,
                                final String refreshToken,
                                final Long refreshTokenExpiresIn,
                                final MapApi mapApi,
                                final ProviderType providerType) {
            this.accessToken = accessToken;
            this.accessTokenExpiresIn = accessTokenExpiresIn;
            this.refreshToken = refreshToken;
            this.refreshTokenExpiresIn = refreshTokenExpiresIn;
            this.map = mapApi != null ? new Map(mapApi.getName(), mapApi.getUrlScheme()) : null;
            this.socialType = providerType.name();
        }

        public AuthInfoResponse(final ProviderType providerType,
                                final Boolean newUser,
                                final String email,
                                final String nickname) {
            this.accessToken = null;
            this.accessTokenExpiresIn = null;
            this.refreshToken = null;
            this.refreshTokenExpiresIn = null;
            this.map = null;
            this.socialType = providerType.name();
            this.newUser = newUser;
            this.userInfo = new UserInfo(email, nickname);
        }

        @Getter
        @RequiredArgsConstructor
        @JsonInclude(Include.NON_NULL)
        @Schema(name = "지도 API 정보", description = "현재 사용자의 주사용 지도 API 관련 정보")
        static class Map {

            @Schema(description = "지도 API 이름")
            private final String mapName;
            @Schema(description = "지도 API URL 스키마", nullable = true)
            private final String mapUrlScheme;

        }

        @Getter
        @RequiredArgsConstructor
        @JsonInclude(Include.NON_NULL)
        @Schema(name = "사용자 정보", description = "소셜 ID 토큰 내 사용자 정보")
        static class UserInfo {

            @Schema(description = "이메일")
            private final String email;
            @Schema(description = "닉네임")
            private final String nickname;

        }

    }

}
