package com.timespot.backend.domain.station.dao;

import com.timespot.backend.domain.station.dto.StationResponseDto.StationProjection;
import com.timespot.backend.domain.station.model.Station;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * PackageName : com.timespot.backend.domain.station.dao
 * FileName    : StationRepository
 * Author      : whitecity01
 * Date        : 26. 3. 26.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 26.     whitecity01       Initial creation
 * 26. 3. 26.     loadingKKamo21      StationRepositoryCustom 추가
 */
public interface StationRepository extends JpaRepository<Station, Long>, StationRepositoryCustom {

    // 모든 역 정보 및 사용자의 즐겨찾기 여부 조회
    @Query(value =
                   "SELECT s.station_id AS stationId, s.name AS name, s.line_name AS lineName, " +
                   "IF(f.station_id IS NOT NULL, TRUE, FALSE) AS isFavorite " +
                   "FROM stations s " +
                   "LEFT JOIN favorite_stations f ON s.station_id = f.station_id AND f.user_id = :userId " +
                   "WHERE s.is_active = 1",
           nativeQuery = true)
    List<StationProjection> findAllWithFavoriteStatus(@Param("userId") byte[] userId);

    // 내 위치 기반 가까운 역 2개 및 즐겨찾기 여부 조회
    @Query(value =
                   "SELECT s.station_id AS stationId, s.name AS name, s.line_name AS lineName, " +
                   "IF(f.station_id IS NOT NULL, TRUE, FALSE) AS isFavorite " +
                   "FROM stations s " +
                   "LEFT JOIN favorite_stations f ON s.station_id = f.station_id AND f.user_id = :userId " +
                   "WHERE s.is_active = 1 " +
                   "ORDER BY ST_Distance_Sphere(s.location, ST_GeomFromText(CONCAT('POINT(', :lat, ' ', :lng, ')'), " +
                   "4326)) ASC " +
                   "LIMIT 2",
           nativeQuery = true)
    List<StationProjection> findNearbyStationsWithFavoriteStatus(
            @Param("userId") byte[] userId,
            @Param("lat") double lat,
            @Param("lng") double lng);
}