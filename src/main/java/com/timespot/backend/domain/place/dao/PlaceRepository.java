package com.timespot.backend.domain.place.dao;

import com.timespot.backend.domain.place.dto.PlaceResponseDto;
import com.timespot.backend.domain.place.model.Place;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


/**
 * PackageName : com.timespot.backend.domain.place.dao
 * FileName    : PlaceRepository
 * Author      : whitecity01
 * Date        : 26. 3. 19.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 19.     whitecity01       Initial creation
 * 26. 3. 22.     whitecity01       ADD pagenation
 */
public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query(value = """
            SELECT 
                p.name AS name,
                p.google_place_id AS googlePlaceId,
                p.category AS category,
                p.address AS address,
                ST_Y(p.location) AS lat,
                ST_X(p.location) AS lon,
                FLOOR(
                    (
                        :walkableDistance - (
                            ST_Distance_Sphere(p.location, ST_GeomFromText(CONCAT('POINT(', :userLat, ' ', :userLon, ')'), 4326)) +
                            ST_Distance_Sphere(p.location, ST_GeomFromText(CONCAT('POINT(', :stationLat, ' ', :stationLon, ')'), 4326))
                        )
                    ) / :walkSpeed
                ) AS stayableMinutes,
                COUNT(*) OVER() AS totalCount
            FROM places p
            -- 1. 출발 역 근처 장소만 추출
            INNER JOIN station_place_map spm ON p.place_id = spm.place_id
            WHERE spm.station_id = :stationId
              AND 
            -- 2. 추출된 장소들에 대해서만 동선 거리 계산 (사용자->장소 + 장소->역) <= 남은 시간
                (
                    ST_Distance_Sphere(
                        p.location, 
                        ST_GeomFromText(CONCAT('POINT(', :userLat, ' ', :userLon, ')'), 4326)
                    ) 
                    + 
                    ST_Distance_Sphere(
                        p.location, 
                        ST_GeomFromText(CONCAT('POINT(', :stationLat, ' ', :stationLon, ')'), 4326)
                    )
                ) <= :walkableDistance
            """, nativeQuery = true)
    List<PlaceResponseDto.AvailablePlace> findAvailablePlacesOnRoute(
            @Param("stationId") Long stationId,
            @Param("userLat") double userLat,
            @Param("userLon") double userLon,
            @Param("stationLat") double stationLat,
            @Param("stationLon") double stationLon,
            @Param("walkableDistance") int walkableDistance,
            @Param("walkSpeed") int walkSpeed,
            Pageable pageable
    );

    @Query(value = """
            SELECT 
                p.name AS name,
                p.category AS category,
                p.address AS address,
                ST_Distance_Sphere(p.location, s.location) AS distanceToStation,
                FLOOR(ST_Distance_Sphere(p.location, s.location) / :walkSpeed) AS timeToStation
            FROM places p
            INNER JOIN station_place_map spm ON p.place_id = spm.place_id
            INNER JOIN stations s ON spm.station_id = s.station_id 
            WHERE p.google_place_id = :googleId
              AND s.station_id = :stationId
            LIMIT 1
            """, nativeQuery = true)
    Optional<PlaceResponseDto.PlaceDetailInDB> findPlaceDetail(
            @Param("googleId") String googleId,
            @Param("stationId") Long stationId,
            @Param("walkSpeed") int walkSpeed
    );
}