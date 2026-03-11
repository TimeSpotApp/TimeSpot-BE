package com.timespot.backend.domain.user.dao;

import com.timespot.backend.domain.user.dto.UserResponseDto.UserInfoResponse;
import java.util.Optional;
import java.util.UUID;

/**
 * PackageName : com.timespot.backend.domain.user.dao
 * FileName    : UserRepositoryCustom
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
public interface UserRepositoryCustom {

    Optional<UserInfoResponse> findUserInfoById(UUID id);

}
