package com.timespot.backend.domain.favorite.service;

import com.timespot.backend.domain.favorite.dto.FavoriteResponseDto.FavoriteListResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * PackageName : com.timespot.backend.domain.favorite.service
 * FileName    : FavoriteService
 * Author      : loadingKKamo21
 * Date        : 26. 3. 25.
 * Description : 즐겨찾기 도메인 서비스 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 25.    loadingKKamo21       Initial creation
 */
public interface FavoriteService {

    /**
     * 즐겨찾기 역 생성
     *
     * @param userId    사용자 ID
     * @param stationId 역 ID
     */
    void createFavoriteStation(UUID userId, Long stationId);

    /**
     * 즐겨찾기 역 삭제
     *
     * @param userId     사용자 ID
     * @param favoriteId 즐겨찾기 ID
     */
    void deleteFavoriteStation(UUID userId, Long favoriteId);

    /**
     * 즐겨찾기 역 목록 조회
     *
     * @param userId   사용자 ID
     * @param keyword  검색어 (역 이름)
     * @param pageable 페이지네이션 정보
     * @return 즐겨찾기 역 목록
     */
    Page<FavoriteListResponse> getFavoriteStationList(UUID userId, String keyword, Pageable pageable);

}
