package com.timespot.backend.domain.place.dao;

import com.timespot.backend.domain.place.dto.PlaceResponseDto;
import com.timespot.backend.domain.place.model.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
 */
public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query(value = """
            SELECT 
                p.name AS name,
                p.google_place_id AS googlePlaceId,
                p.category AS category,
                p.address AS address,
                ST_Y(p.location) AS lat,
                ST_X(p.location) AS lon
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
            @Param("walkableDistance") int walkableDistance
    );
}