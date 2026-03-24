package com.timespot.backend.domain.user.dao;

import com.timespot.backend.domain.user.dto.UserResponseDto.UserInfoResponse;
import java.util.Optional;
import java.util.UUID;

/**
 * PackageName : com.timespot.backend.domain.user.dao
 * FileName    : UserRepositoryCustom
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description : 사용자 리포지토리 커스텀 인터페이스 (QueryDSL 활용)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
public interface UserRepositoryCustom {

    /**
     * 회원 ID 로 회원 정보 조회 (DTO)
     *
     * @param id 회원 ID
     * @return 회원 정보 응답 DTO (Optional)
     */
    Optional<UserInfoResponse> findUserInfoById(UUID id);

}
