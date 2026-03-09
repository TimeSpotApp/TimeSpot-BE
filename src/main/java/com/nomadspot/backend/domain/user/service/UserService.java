package com.nomadspot.backend.domain.user.service;

import com.nomadspot.backend.domain.user.model.ProviderType;
import com.nomadspot.backend.domain.user.model.User;
import java.util.UUID;

/**
 * PackageName : com.nomadspot.backend.domain.user.service
 * FileName    : UserService
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
public interface UserService {

    User findOrCreateUserForSocialConnection(ProviderType providerType,
                                             String providerUserId,
                                             String email,
                                             String nickname);

    User findById(UUID id);

}
