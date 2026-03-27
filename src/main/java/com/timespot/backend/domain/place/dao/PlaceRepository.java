package com.timespot.backend.domain.place.dao;

import com.timespot.backend.domain.place.dto.PlaceResponseDto;
import com.timespot.backend.domain.place.model.Place;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
 * 26. 3. 26.     whitecity01       MODIFY findAvailablePlacesOnRoute logic
 * 26. 3. 27.     whitecity01       ADD place search
 * 26. 3. 27.     whitecity01       MODIFY getPlaceDetail response
 */
public interface PlaceRepository extends JpaRepository<Place, Long> {

    // 화면 기준 300m 내의 장소 중 사용자 체류 가능 장소 조회
    @Query(value = """
            SELECT 
                p.place_id AS placeId,
                ST_Y(p.location) AS lat,
                ST_X(p.location) AS lon,
                p.category AS category
            FROM places p
            INNER JOIN station_place_map spm ON p.place_id = spm.place_id
            WHERE spm.station_id = :stationId
              AND ST_Distance_Sphere(p.location, ST_GeomFromText(CONCAT('POINT(', :mapLat, ' ', :mapLon, ')'), 4326)) <= 300
              AND (
                    ST_Distance_Sphere(p.location, ST_GeomFromText(CONCAT('POINT(', :userLat, ' ', :userLon, ')'), 4326)) 
                    + 
                    ST_Distance_Sphere(p.location, ST_GeomFromText(CONCAT('POINT(', :stationLat, ' ', :stationLon, ')'), 4326))
              ) <= :walkableDistance
            """, nativeQuery = true)
    List<PlaceResponseDto.SimpleAvailablePlace> findAvailablePlacesOnRoute(
            @Param("stationId") Long stationId,
            @Param("userLat") double userLat,
            @Param("userLon") double userLon,
            @Param("stationLat") double stationLat,
            @Param("stationLon") double stationLon,
            @Param("mapLat") double mapLat,
            @Param("mapLon") double mapLon,
            @Param("walkableDistance") int walkableDistance,
            @Param("walkSpeed") int walkSpeed
    );

    @Query(value = """
            SELECT 
                p.name AS name,
                p.category AS category,
                p.address AS address,
                CAST(ROUND(ST_Distance_Sphere(p.location, s.location)) AS UNSIGNED) AS distanceToStation,
                FLOOR(ST_Distance_Sphere(p.location, s.location) / :walkSpeed) AS timeToStation,
                FLOOR(
                    (
                        :walkableDistance - (
                            ST_Distance_Sphere(p.location, ST_GeomFromText(CONCAT('POINT(', :userLat, ' ', :userLon, ')'), 4326)) +
                            ST_Distance_Sphere(p.location, s.location)
                        )
                    ) / :walkSpeed
                ) AS stayableMinutes
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
            @Param("userLat") double userLat,
            @Param("userLon") double userLon,
            @Param("walkableDistance") int walkableDistance,
            @Param("walkSpeed") int walkSpeed
    );

    @Query(value = """
            SELECT 
                p.name AS name,
                p.google_place_id AS googlePlaceId,
                p.category AS category,
                p.address AS address,
                ST_X(p.location) AS lat,
                ST_Y(p.location) AS lon,
                FLOOR(
                    (
                        :walkableDistance - (
                            ST_Distance_Sphere(p.location, ST_GeomFromText(CONCAT('POINT(', :userLat, ' ', :userLon, ')'), 4326)) +
                            ST_Distance_Sphere(p.location, ST_GeomFromText(CONCAT('POINT(', :stationLat, ' ', :stationLon, ')'), 4326))
                        )
                    ) / :walkSpeed
                ) AS stayableMinutes,
                ST_Distance_Sphere(p.location, ST_GeomFromText(CONCAT('POINT(', :stationLat, ' ', :stationLon, ')'), 4326)) AS distanceToStation,
                ST_Distance_Sphere(p.location, ST_GeomFromText(CONCAT('POINT(', :userLat, ' ', :userLon, ')'), 4326)) AS distanceToUser,
                ST_Distance_Sphere(p.location, ST_GeomFromText(CONCAT('POINT(', :markerLat, ' ', :markerLon, ')'), 4326)) AS distanceToMarker
            FROM places p
            INNER JOIN station_place_map spm ON p.place_id = spm.place_id
            WHERE spm.station_id = :stationId
              AND (:keyword IS NULL OR p.name LIKE CONCAT('%', :keyword, '%'))
              AND (:category IS NULL OR p.category = :category)
              AND (
                    ST_Distance_Sphere(p.location, ST_GeomFromText(CONCAT('POINT(', :userLat, ' ', :userLon, ')'), 4326)) 
                    + 
                    ST_Distance_Sphere(p.location, ST_GeomFromText(CONCAT('POINT(', :stationLat, ' ', :stationLon, ')'), 4326))
              ) <= :walkableDistance
            ORDER BY
                CASE WHEN :sortBy = 'USER_NEAREST' THEN distanceToUser
                     WHEN :sortBy = 'MARKER_NEAREST' THEN distanceToMarker
                     ELSE distanceToStation
                END ASC
            """, nativeQuery = true)
    Slice<PlaceResponseDto.AvailablePlace> searchAvailablePlaces(
            @Param("stationId") Long stationId,
            @Param("userLat") double userLat,
            @Param("userLon") double userLon,
            @Param("stationLat") double stationLat,
            @Param("stationLon") double stationLon,
            @Param("markerLat") double markerLat,
            @Param("markerLon") double markerLon,
            @Param("walkableDistance") int walkableDistance,
            @Param("walkSpeed") int walkSpeed,
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("sortBy") String sortBy,
            Pageable pageable
    );
}