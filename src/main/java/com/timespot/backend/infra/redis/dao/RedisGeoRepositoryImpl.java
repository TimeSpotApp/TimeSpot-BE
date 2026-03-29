package com.timespot.backend.infra.redis.dao;

import com.timespot.backend.infra.redis.model.GeoPlace;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * PackageName : com.timespot.backend.infra.redis.dao
 * FileName    : RedisGeoRepositoryImpl
 * Author      : loadingKKamo21
 * Date        : 26. 3. 29.
 * Description: Redis GEO 저장소 구현체
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 29.    loadingKKamo21       Initial creation
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class RedisGeoRepositoryImpl implements RedisGeoRepository {

    private final RedisTemplate<String, String> redisTemplateForGeo;

    @Override
    public void addPlace(final String key,
                         final String placeId,
                         final double longitude,
                         final double latitude) {
        log.debug("GEO 에 장소 추가: key={}, placeId={}, lat={}, lon={}",
                  key, placeId, latitude, longitude);

        redisTemplateForGeo.opsForGeo().add(
                key,
                new Point(longitude, latitude), // Redis 는 (경도, 위도) 순서
                placeId
        );
    }

    @Override
    public List<GeoPlace> findPlacesWithinRadius(final String key,
                                                 final double longitude,
                                                 final double latitude,
                                                 final double radiusMeters) {
        Circle within = new Circle(
                new Point(longitude, latitude),
                new Distance(radiusMeters / 1000.0, Metrics.KILOMETERS)
        );

        log.debug("GEO 검색: key={}, lat={}, lon={}, radius={}m", key, latitude, longitude, radiusMeters);

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplateForGeo.opsForGeo().search(key, within);

        if (results == null || results.getContent().isEmpty()) {
            log.debug("GEO 검색 결과 없음: key={}", key);
            return List.of();
        }

        return results.getContent().stream()
                      .map(this::mapToGeoPlace)
                      .filter(Objects::nonNull)
                      .toList();
    }

    @Override
    public void deleteGeoKey(final String key) {
        log.debug("GEO 키 삭제: key={}", key);
        redisTemplateForGeo.delete(key);
    }

    // ========================= 내부 메서드 =========================

    /**
     * GeoResult 를 GeoPlace 로 매핑
     */
    private GeoPlace mapToGeoPlace(final GeoResult<RedisGeoCommands.GeoLocation<String>> result) {
        try {
            RedisGeoCommands.GeoLocation<String> content = result.getContent();
            String                               placeId = content.getName();

            Point point = content.getPoint();
            if (point == null) return null;

            double distanceInMeters = result.getDistance().getValue() * 1000;

            return GeoPlace.builder()
                           .placeId(placeId)
                           .latitude(point.getY())  // Y = latitude
                           .longitude(point.getX()) // X = longitude
                           .distance(distanceInMeters)
                           .build();
        } catch (Exception e) {
            log.error("GeoPlace 매핑 실패: {}", e.getMessage());
            return null;
        }
    }

}
