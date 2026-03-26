package com.timespot.backend.domain.user.dao;

import com.timespot.backend.domain.user.model.ProviderType;
import com.timespot.backend.domain.user.model.SocialConnection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PackageName : com.timespot.backend.domain.user.dao
 * FileName    : SocialConnectionRepository
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description : 소셜 연결 리포지토리 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
public interface SocialConnectionRepository extends JpaRepository<SocialConnection, Long> {

    Optional<SocialConnection> findByProviderTypeAndProviderId(ProviderType providerType, String providerId);

    boolean existsByProviderTypeAndProviderId(ProviderType providerType, String providerId);

    Optional<SocialConnection> findByUserId(UUID userId);

}
