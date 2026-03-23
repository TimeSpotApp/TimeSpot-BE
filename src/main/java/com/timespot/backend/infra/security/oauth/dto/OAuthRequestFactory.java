package com.timespot.backend.infra.security.oauth.dto;

import static com.timespot.backend.infra.security.oauth.constant.OAuthConst.CLIENT_ID;
import static com.timespot.backend.infra.security.oauth.constant.OAuthConst.CLIENT_SECRET;
import static com.timespot.backend.infra.security.oauth.constant.OAuthConst.CODE;
import static com.timespot.backend.infra.security.oauth.constant.OAuthConst.GRANT_TYPE;
import static com.timespot.backend.infra.security.oauth.constant.OAuthConst.REDIRECT_URI;
import static com.timespot.backend.infra.security.oauth.constant.OAuthConst.REFRESH_TOKEN;
import static com.timespot.backend.infra.security.oauth.constant.OAuthConst.TOKEN;
import static com.timespot.backend.infra.security.oauth.constant.OAuthConst.TOKEN_TYPE_HINT;

import com.timespot.backend.infra.security.oauth.constant.OAuthConst;
import com.timespot.backend.infra.security.oauth.constant.TokenType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * PackageName : com.timespot.backend.infra.security.oauth.dto
 * FileName    : OAuthRequestFactory
 * Author      : loadingKKamo21
 * Date        : 26. 3. 12.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 12.    loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class OAuthRequestFactory {

    /**
     * Apple 인증 코드 교환 요청 파라미터 생성
     *
     * @param clientId          Apple 클라이언트 ID
     * @param clientSecret      Apple 클라이언트 시크릿
     * @param authorizationCode Apple 인증 코드
     * @param redirectUri       Apple 리디렉션 URI
     * @return Apple 인증 코드 교환 요청 파라미터
     */
    public static MultiValueMap<String, String> createAppleTokenValidationRequest(
            @NotBlank final String clientId,
            @NotBlank final String clientSecret,
            @NotBlank final String authorizationCode,
            final String redirectUri
    ) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(CLIENT_ID, clientId);
        params.add(CLIENT_SECRET, clientSecret);
        params.add(CODE, authorizationCode);
        params.add(GRANT_TYPE, OAuthConst.APPLE_IDP_TOKEN_AUTHORIZATION_GRANT_TYPE);
        params.add(REDIRECT_URI, redirectUri);
        return params;
    }

    /**
     * Apple 인증 토큰 갱신 요청 파라미터 생성
     *
     * @param clientId     Apple 클라이언트 ID
     * @param clientSecret Apple 클라이언트 시크릿
     * @param refreshToken Apple 리프레시 토큰
     * @return Apple 인증 토큰 갱신 요청 파라미터
     */
    public static MultiValueMap<String, String> createAppleTokenRefreshRequest(
            @NotBlank final String clientId,
            @NotBlank final String clientSecret,
            @NotBlank final String refreshToken
    ) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(CLIENT_ID, clientId);
        params.add(CLIENT_SECRET, clientSecret);
        params.add(GRANT_TYPE, OAuthConst.APPLE_IDP_TOKEN_REFRESH_GRANT_TYPE);
        params.add(REFRESH_TOKEN, refreshToken);
        return params;
    }

    /**
     * Apple 인증 토큰 폐기 요청 파라미터 생성
     *
     * @param clientId     Apple 클라이언트 ID
     * @param clientSecret Apple 클라이언트 시크릿
     * @param tokenType    토큰 유형: access_token, refresh_token
     * @param token        토큰
     * @return Apple 인증 토큰 폐기 요청 파라미터
     */
    public static MultiValueMap<String, String> createAppleTokenRevokeRequest(
            @NotBlank final String clientId,
            @NotBlank final String clientSecret,
            @NotNull final TokenType tokenType,
            @NotBlank final String token
    ) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(CLIENT_ID, clientId);
        params.add(CLIENT_SECRET, clientSecret);
        params.add(TOKEN, token);
        params.add(TOKEN_TYPE_HINT, tokenType.getValue());
        return params;
    }

    /**
     * Google 인증 코드 교환 요청 파라미터 생성
     *
     * @param clientId          Google 클라이언트 ID
     * @param clientSecret      Google 클라이언트 시크릿
     * @param authorizationCode Google 인증 코드
     * @param redirectUri       Google 리디렉션 URI
     * @return Google 인증 코드 교환 요청 파라미터
     */
    public static MultiValueMap<String, String> createGoogleTokenValidationRequest(
            @NotBlank final String clientId,
            @NotBlank final String clientSecret,
            @NotBlank final String authorizationCode,
            @NotBlank final String redirectUri
    ) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(CLIENT_ID, clientId);
        params.add(CLIENT_SECRET, clientSecret);
        params.add(CODE, authorizationCode);
        params.add(GRANT_TYPE, OAuthConst.GOOGLE_IDP_TOKEN_AUTHORIZATION_GRANT_TYPE);
        params.add(REDIRECT_URI, redirectUri);
        return params;
    }

    /**
     * Google 인증 토큰 갱신 요청 파라미터 생성
     *
     * @param clientId     Google 클라이언트 ID
     * @param clientSecret Google 클라이언트 시크릿
     * @param refreshToken Google 리프레시 토큰
     * @return Google 인증 토큰 갱신 요청 파라미터
     */
    public static MultiValueMap<String, String> createGoogleTokenRefreshRequest(
            @NotBlank final String clientId,
            @NotBlank final String clientSecret,
            @NotBlank final String refreshToken
    ) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(CLIENT_ID, clientId);
        params.add(CLIENT_SECRET, clientSecret);
        params.add(GRANT_TYPE, OAuthConst.GOOGLE_IDP_TOKEN_REFRESH_GRANT_TYPE);
        params.add(REFRESH_TOKEN, refreshToken);
        return params;
    }

}
