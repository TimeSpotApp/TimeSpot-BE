package com.nomadspot.backend.infra.security.oauth.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.nomadspot.backend.infra.security.oauth.constant
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

    public static final String APPLE_JWKS_URL  = "https://appleid.apple.com/auth/keys";
    public static final String GOOGLE_JWKS_URL = "https://www.googleapis.com/oauth2/v3/certs";

}
