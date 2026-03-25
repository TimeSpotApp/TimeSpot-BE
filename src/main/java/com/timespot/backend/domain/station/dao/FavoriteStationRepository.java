package com.timespot.backend.domain.station.dao;

import com.timespot.backend.domain.station.dto.FavoriteStation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * PackageName : com.timespot.backend.domain.station.dao
 * FileName    : FavoriteStationRepository
 * Author      : whitecity01
 * Date        : 26. 3. 26.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 26.     whitecity01       Initial creation
 */
public interface FavoriteStationRepository extends JpaRepository<FavoriteStation, Long> {
    boolean existsByUserIdAndStationId(UUID userId, Long stationId);
    void deleteByUserIdAndStationId(UUID userId, Long stationId);
}