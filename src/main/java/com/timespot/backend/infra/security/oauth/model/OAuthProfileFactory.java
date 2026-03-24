package com.timespot.backend.infra.security.oauth.model;

import static lombok.AccessLevel.PRIVATE;

import com.timespot.backend.domain.user.model.ProviderType;
import java.util.Map;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.infra.security.oauth.model
 * FileName    : OAuthProfileFactory
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
public final class OAuthProfileFactory {

    public static OAuthProfile getOAuthProfile(final String registrationId, final Map<String, Object> attributes) {
        return switch (ProviderType.from(registrationId)) {
            case APPLE -> new AppleProfile(attributes);
            case GOOGLE -> new GoogleProfile(attributes);
        };
    }

}
