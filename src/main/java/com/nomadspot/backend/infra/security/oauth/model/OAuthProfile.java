package com.nomadspot.backend.infra.security.oauth.model;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.nomadspot.backend.infra.security.oauth.model
 * FileName    : OAuthProfile
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class OAuthProfile {

    private final Map<String, Object> attributes;

    public abstract String getProvider();

    public abstract String getProviderUserId();

    public abstract String getEmail();

    public abstract String getNickname();

}
