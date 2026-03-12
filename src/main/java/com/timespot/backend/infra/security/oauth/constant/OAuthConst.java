package com.timespot.backend.infra.security.oauth.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.infra.security.oauth.constant
 * FileName    : OAuthConst
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class OAuthConst {

    public static final String APPLE_JWKS_URL                           = "https://appleid.apple.com/auth/keys";
    public static final String APPLE_ISSUER                             = "https://appleid.apple.com";
    public static final String APPLE_IDP_TOKEN_AUTHORIZATION_GRANT_TYPE = "authorization_code";
    public static final String APPLE_IDP_TOKEN_REFRESH_GRANT_TYPE       = "refresh_token";
    public static final String APPLE_IDP_TOKEN_URL                      = "https://appleid.apple.com/auth/token";
    public static final String APPLE_IDP_TOKEN_REDIRECT_URL             = "";

    public static final String GOOGLE_JWKS_URL                           = "https://www.googleapis.com/oauth2/v3/certs";
    public static final String GOOGLE_ISSUER                             = "https://accounts.google.com";
    public static final String GOOGLE_IDP_TOKEN_AUTHORIZATION_GRANT_TYPE = "authorization_code";
    public static final String GOOGLE_IDP_TOKEN_REFRESH_GRANT_TYPE       = "refresh_token";
    public static final String GOOGLE_IDP_TOKEN_URL                      = "https://oauth2.googleapis.com/token";
    public static final String GOOGLE_IDP_TOKEN_REDIRECT_URL             = "";

}
