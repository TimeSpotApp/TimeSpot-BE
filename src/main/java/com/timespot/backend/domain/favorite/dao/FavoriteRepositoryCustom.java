package com.timespot.backend.domain.favorite.dao;

import com.timespot.backend.domain.favorite.dto.FavoriteResponseDto.FavoriteListResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * PackageName : com.timespot.backend.domain.favorite.dao
 * FileName    : FavoriteRepositoryCustom
 * Author      : loadingKKamo21
 * Date        : 26. 3. 24.
 * Description : 즐겨찾기 리포지토리 커스텀 인터페이스 (QueryDSL)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 24.    loadingKKamo21       Initial creation
 */
public interface FavoriteRepositoryCustom {

    /**
     * 즐겨찾기 역 목록 조회
     *
     * @param userId   사용자 ID
     * @param keyword  검색어 (역 이름, 선택)
     * @param pageable 페이지네이션 정보
     * @return 즐겨찾기 역 목록
     */
    Page<FavoriteListResponse> findFavoriteStationList(UUID userId, String keyword, Pageable pageable);

}
