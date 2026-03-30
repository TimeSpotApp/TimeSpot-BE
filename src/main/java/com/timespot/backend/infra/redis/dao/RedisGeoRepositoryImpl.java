package com.timespot.backend.infra.redis.dao;

import com.timespot.backend.infra.redis.model.GeoPlace;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoRadiusCommandArgs;
import org.springframework.data.redis.core.RedisCallback;
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

        try {
            Long result = redisTemplateForGeo.opsForGeo().add(
                    key, new Point(longitude, latitude), placeId
            );
            if (result != null && result > 0)
                log.debug("GEO 저장 완료: key={}, placeId={}, addedCount={}", key, placeId, result);
            else if (result != null && result == 0)
                log.debug("GEO 저장: 기존 멤버 위치 업데이트 - key={}, placeId={}", key, placeId);
        } catch (Exception e) {
            log.error("GEO 저장 실패: key={}, placeId={}, lat={}, lon={}, error={}",
                      key, placeId, latitude, longitude, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<GeoPlace> findPlacesWithinRadius(final String key,
                                                 final double longitude,
                                                 final double latitude,
                                                 final double radiusMeters) {
        try {
            log.debug("GEO 검색: key={}, lat={}, lon={}, radius={}m", key, latitude, longitude, radiusMeters);

            GeoResults<GeoLocation<byte[]>> geoResults = redisTemplateForGeo.execute(
                    (RedisCallback<GeoResults<GeoLocation<byte[]>>>) connection -> {
                        Circle circle = new Circle(
                                new Point(longitude, latitude),
                                new Distance(radiusMeters / 1000.0, Metrics.KILOMETERS)
                        );
                        return connection.geoCommands().geoRadius(
                                key.getBytes(),
                                circle,
                                GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates().includeDistance()
                        );
                    }
            );

            if (geoResults == null || geoResults.getContent() == null || geoResults.getContent().isEmpty()) {
                log.debug("GEO 검색 결과 없음: key={}", key);
                return List.of();
            }

            List<GeoPlace> results = geoResults.getContent().stream()
                                               .map(this::mapToGeoPlace)
                                               .filter(Objects::nonNull)
                                               .collect(Collectors.toList());

            log.debug("GEO 검색 결과: key={}, count={}", key, results.size());
            return results;
        } catch (Exception e) {
            log.error("GEO 검색 실패: key={}, lat={}, lon={}, radius={}m, error={}",
                      key, latitude, longitude, radiusMeters, e.getMessage(), e);
            throw e;
        }
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
    private GeoPlace mapToGeoPlace(final GeoResult<GeoLocation<byte[]>> result) {
        try {
            GeoLocation<byte[]> content = result.getContent();
            String              placeId = new String(content.getName());

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
