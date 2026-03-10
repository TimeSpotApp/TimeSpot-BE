package com.nomadspot.backend.infra.security.oauth.model;

import com.nomadspot.backend.domain.user.model.ProviderType;
import java.util.Map;

/**
 * PackageName : com.nomadspot.backend.infra.security.oauth.model
 * FileName    : GoogleProfile
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
public class GoogleProfile extends OAuthProfile {

    protected GoogleProfile(final Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getProvider() {
        return ProviderType.GOOGLE.name();
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
        return (String) getAttributes().get("name");
    }

}
