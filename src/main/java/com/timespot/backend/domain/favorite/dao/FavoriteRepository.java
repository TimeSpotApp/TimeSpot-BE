package com.timespot.backend.domain.favorite.dao;

import com.timespot.backend.domain.favorite.model.Favorite;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PackageName : com.timespot.backend.domain.favorite.dao
 * FileName    : FavoriteRepository
 * Author      : loadingKKamo21
 * Date        : 26. 3. 24.
 * Description : 즐겨찾기 리포지토리 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 24.    loadingKKamo21       Initial creation
 */
public interface FavoriteRepository extends JpaRepository<Favorite, Long>, FavoriteRepositoryCustom {

    /**
     * 즐겨찾기 역 존재 여부 확인
     *
     * @param userId    사용자 ID
     * @param stationId 역 ID
     * @return 존재 여부
     */
    boolean existsByUserIdAndStationId(UUID userId, Long stationId);

    /**
     * 사용자 ID 와 역 ID 로 즐겨찾기 조회
     *
     * @param userId    사용자 ID
     * @param stationId 역 ID
     * @return 즐겨찾기 엔티티 (Optional)
     */
    Optional<Favorite> findByUserIdAndStationId(UUID userId, Long stationId);

}
