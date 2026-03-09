package com.nomadspot.backend.infra.security.oauth.model;

import com.nomadspot.backend.common.error.GlobalException;
import com.nomadspot.backend.common.response.ErrorCode;
import com.nomadspot.backend.domain.user.model.ProviderType;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.nomadspot.backend.infra.security.oauth.model
 * FileName    : OAuthProfileFactory
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OAuthProfileFactory {

    public static OAuthProfile getOAuthProfile(final String registrationId, final Map<String, Object> attributes) {
        if (ProviderType.APPLE.name().equalsIgnoreCase(registrationId)) return new AppleProfile(attributes);
        if (ProviderType.GOOGLE.name().equalsIgnoreCase(registrationId)) return new GoogleProfile(attributes);
        throw new GlobalException(ErrorCode.SOCIAL_CONNECTION_PROVIDER_NOT_SUPPORTED);
    }

}
