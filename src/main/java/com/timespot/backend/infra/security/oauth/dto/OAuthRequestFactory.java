package com.timespot.backend.infra.security.oauth.dto;

import com.timespot.backend.infra.security.oauth.constant.OAuthConst;
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
            final String clientId,
            final String clientSecret,
            final String authorizationCode,
            final String redirectUri
    ) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", authorizationCode);
        params.add("grant_type", OAuthConst.APPLE_IDP_TOKEN_AUTHORIZATION_GRANT_TYPE);
        params.add("redirect_uri", redirectUri);
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
            final String clientId,
            final String clientSecret,
            final String refreshToken
    ) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("grant_type", OAuthConst.APPLE_IDP_TOKEN_REFRESH_GRANT_TYPE);
        params.add("refresh_token", refreshToken);
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
            final String clientId,
            final String clientSecret,
            final String authorizationCode,
            final String redirectUri
    ) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", authorizationCode);
        params.add("grant_type", OAuthConst.GOOGLE_IDP_TOKEN_AUTHORIZATION_GRANT_TYPE);
        params.add("redirect_uri", redirectUri);
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
            final String clientId,
            final String clientSecret,
            final String refreshToken
    ) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("grant_type", OAuthConst.GOOGLE_IDP_TOKEN_REFRESH_GRANT_TYPE);
        params.add("refresh_token", refreshToken);
        return params;
    }

}
