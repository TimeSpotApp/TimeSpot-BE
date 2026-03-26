package com.timespot.backend.domain.user.dao;

import com.timespot.backend.domain.user.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PackageName : com.timespot.backend.domain.user.dao
 * FileName    : UserRepository
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description : 사용자 리포지토리 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
public interface UserRepository extends JpaRepository<User, UUID>, UserRepositoryCustom {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

}
