package com.timespot.backend.infra.security.oauth.model;

import static com.timespot.backend.domain.user.model.ProviderType.APPLE;

import java.util.Map;

/**
 * PackageName : com.timespot.backend.infra.security.oauth.model
 * FileName    : AppleProfile
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
public class AppleProfile extends OAuthProfile {

    protected AppleProfile(final Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getProvider() {
        return APPLE.name();
    }

    @Override
    public String getProviderUserId() {
        return (String) getAttributes().get("sub");
    }

    @Override
    public String getEmail() {
        return (String) getAttributes().get("email");
    }

    @Override
    public String getNickname() {
        return null;
    }

}
