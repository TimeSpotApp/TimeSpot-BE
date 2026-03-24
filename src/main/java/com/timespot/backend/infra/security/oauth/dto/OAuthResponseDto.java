package com.timespot.backend.infra.security.oauth.dto;

import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.infra.security.oauth.dto
 * FileName    : OAuthResponseDto
 * Author      : loadingKKamo21
 * Date        : 26. 3. 12.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 12.    loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
public abstract class OAuthResponseDto {

    public record AppleTokenValidationResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") Long expiresIn,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("id_token") String idToken
    ) {}

    public record AppleTokenRefreshResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") Long expiresIn,
            @JsonProperty("id_token") String idToken
    ) {}

    public record GoogleTokenValidationResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") Long expiresIn,
            @JsonProperty("id_token") String idToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("scope") String scope,
            @JsonProperty("refresh_token") String refreshToken
    ) {}

    public record GoogleTokenRefreshResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") Long expiresIn,
            @JsonProperty("scope") String scope,
            @JsonProperty("token_type") String tokenType
    ) {}

}
