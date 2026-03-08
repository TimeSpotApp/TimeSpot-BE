package com.nomadspot.backend.domain.user.dao;

import com.nomadspot.backend.domain.user.model.ProviderType;
import com.nomadspot.backend.domain.user.model.SocialConnection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PackageName : com.nomadspot.backend.domain.user.dao
 * FileName    : SocialConnectionRepository
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
public interface SocialConnectionRepository extends JpaRepository<SocialConnection, Long> {

    Optional<SocialConnection> findByProviderTypeAndProviderId(ProviderType providerType, String providerId);

    boolean existsByProviderTypeAndProviderId(ProviderType providerType, String providerId);

}
