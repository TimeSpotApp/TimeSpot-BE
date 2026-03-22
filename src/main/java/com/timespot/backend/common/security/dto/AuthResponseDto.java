package com.timespot.backend.common.security.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.timespot.backend.domain.user.model.MapApi;
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
        private final String  accessToken;
        @Schema(description = "Access Token 만료 시간(초)")
        private final long    accessTokenExpiresIn;
        @Schema(description = "Refresh Token")
        private final String  refreshToken;
        @Schema(description = "Refresh Token 만료 시간(초)")
        private final long    refreshTokenExpiresIn;
        @Schema(description = "회원의 주사용 지도 API 정보")
        private       Map     map;
        @Schema(description = "신규 회원 여부", nullable = true)
        private       Boolean newUser = null;

        public AuthInfoResponse(final String accessToken,
                                final long accessTokenExpiresIn,
                                final String refreshToken,
                                final long refreshTokenExpiresIn,
                                final MapApi mapApi) {
            this.accessToken = accessToken;
            this.accessTokenExpiresIn = accessTokenExpiresIn;
            this.refreshToken = refreshToken;
            this.refreshTokenExpiresIn = refreshTokenExpiresIn;
            this.map = new Map(mapApi.getName(), mapApi.getUrlScheme());
        }

        public AuthInfoResponse(final String accessToken,
                                final long accessTokenExpiresIn,
                                final String refreshToken,
                                final long refreshTokenExpiresIn,
                                final MapApi mapApi,
                                final Boolean newUser) {
            this.accessToken = accessToken;
            this.accessTokenExpiresIn = accessTokenExpiresIn;
            this.refreshToken = refreshToken;
            this.refreshTokenExpiresIn = refreshTokenExpiresIn;
            this.map = new Map(mapApi.getName(), mapApi.getUrlScheme());
            this.newUser = newUser;
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

    }

}
