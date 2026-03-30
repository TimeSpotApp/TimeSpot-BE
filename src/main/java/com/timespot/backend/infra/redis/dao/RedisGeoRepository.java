package com.timespot.backend.infra.redis.dao;

import com.timespot.backend.infra.redis.model.GeoPlace;
import java.util.List;

/**
 * PackageName : com.timespot.backend.infra.redis.dao
 * FileName    : RedisGeoRepository
 * Author      : loadingKKamo21
 * Date        : 26. 3. 29.
 * Description : Redis GEO 저장소 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 29.    loadingKKamo21       Initial creation
 */
public interface RedisGeoRepository {

    /**
     * GEO 에 장소 추가
     *
     * @param key       GEO 키
     * @param placeId   장소 고유 식별자
     * @param longitude 경도
     * @param latitude  위도
     */
    void addPlace(String key, String placeId, double longitude, double latitude);

    /**
     * 특정 반경 내 장소 조회
     *
     * @param key          GEO 키
     * @param longitude    기준 경도
     * @param latitude     기준 위도
     * @param radiusMeters 검색 반경 (미터)
     * @return 반경 내 장소 목록
     */
    List<GeoPlace> findPlacesWithinRadius(String key, double longitude, double latitude, double radiusMeters);

    /**
     * GEO 키 삭제
     *
     * @param key GEO 키
     */
    void deleteGeoKey(String key);

}
