package com.timespot.backend.infra.visitkorea.service;

import com.timespot.backend.infra.redis.model.GeoPlace;
import com.timespot.backend.infra.redis.model.PlaceCardCache;
import com.timespot.backend.infra.redis.model.PlaceDetailCache;
import java.util.List;
import java.util.Optional;

/**
 * PackageName : com.timespot.backend.infra.visitkorea.service
 * FileName    : VisitKoreaPlaceService
 * Author      : loadingKKamo21
 * Date        : 26. 4. 1.
 * Description : 한국관광공사 API 기반 장소 조회 및 캐싱 서비스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 4. 1.     loadingKKamo21       Initial creation
 */
public interface VisitKoreaPlaceService {

    /**
     * 장소 카드 캐시 조회
     *
     * @param placeId 장소 ID (VisitKorea contentId)
     * @return 장소 카드 캐시 (없으면 Optional.empty())
     */
    Optional<PlaceCardCache> getPlaceCardCache(String placeId);

    /**
     * 장소 카드 캐시 저장
     *
     * @param placeId 장소 ID
     * @param cache   저장할 카드 캐시
     */
    void savePlaceCardCache(String placeId, PlaceCardCache cache);

    /**
     * 장소 상세 캐시 조회
     *
     * @param placeId 장소 ID
     * @return 장소 상세 캐시 (없으면 Optional.empty())
     */
    Optional<PlaceDetailCache> getPlaceDetailCache(String placeId);

    /**
     * 장소 상세 캐시 저장
     *
     * @param placeId 장소 ID
     * @param cache   저장할 상세 캐시
     */
    void savePlaceDetailCache(String placeId, PlaceDetailCache cache);

    /**
     * GEO 에 장소 추가
     *
     * @param geoKey    GEO 키 (예: place:geo:{stationId})
     * @param placeId   장소 ID
     * @param longitude 경도
     * @param latitude  위도
     */
    void addPlaceToGeo(String geoKey, String placeId, double longitude, double latitude);

    /**
     * 특정 반경 내 장소 조회
     *
     * @param geoKey       GEO 키
     * @param longitude    기준 경도
     * @param latitude     기준 위도
     * @param radiusMeters 검색 반경 (미터)
     * @return 반경 내 장소 목록
     */
    List<GeoPlace> findPlacesWithinRadius(String geoKey, double longitude, double latitude, double radiusMeters);

    /**
     * GEO 키 삭제
     *
     * @param geoKey GEO 키
     */
    void deleteGeoKey(String geoKey);

    /**
     * VisitKorea API 에서 장소 데이터를 동기화하여 Redis 에 저장
     * <p>
     * - GEO 에 장소 좌표 저장
     * - PlaceCardCache 저장 (TTL: 24 시간)
     * - 기존 캐시가 존재하면 스킵
     * </p>
     *
     * @param stationId    역 ID
     * @param longitude    기준 경도
     * @param latitude     기준 위도
     * @param searchRadius 검색 반경 (미터)
     * @return 동기화된 장소 목록
     */
    List<GeoPlace> syncPlacesFromVisitKorea(Long stationId, double longitude, double latitude, int searchRadius);

    /**
     * 장소 상세 정보 조회 (캐시 미스 시 API 호출)
     * <p>
     * 1. Redis 에서 PlaceCardCache 조회
     * 2. Redis 에서 PlaceDetailCache 조회
     * 3. 캐시가 없으면 VisitKorea API 호출 후 저장
     * </p>
     *
     * @param placeId 장소 ID
     * @return 장소 상세 캐시 (필수 반환, 캐시 미스 시 API 호출)
     * @throws com.timespot.backend.common.error.GlobalException 장소 정보가 존재하지 않는 경우
     */
    PlaceDetailCache getPlaceDetailWithFallback(String placeId);

    /**
     * 장소 카드 정보 조회 (캐시 미스 시 API 호출)
     * <p>
     * 1. Redis 에서 PlaceCardCache 조회
     * 2. 캐시가 없으면 VisitKorea API 호출 후 저장
     * </p>
     *
     * @param placeId 장소 ID
     * @return 장소 카드 캐시 (필수 반환, 캐시 미스 시 API 호출)
     * @throws com.timespot.backend.common.error.GlobalException 장소 정보가 존재하지 않는 경우
     */
    PlaceCardCache getPlaceCardWithFallback(String placeId);

    /**
     * VisitKorea API 에서 장소 데이터를 강제 동기화 (GEO 캐시 유무 무시)
     * <p>
     * 스케줄러에 의한 주기적 갱신용. 기존 GEO 캐시가 있어도 무시하고
     * API 에서 전체 데이터를 다시 가져와 캐시를 갱신합니다.
     * </p>
     *
     * @param stationId    역 ID
     * @param longitude    기준 경도
     * @param latitude     기준 위도
     * @param searchRadius 검색 반경 (미터)
     * @return 동기화된 장소 목록
     */
    List<GeoPlace> forceSyncPlacesFromVisitKorea(Long stationId, double longitude, double latitude, int searchRadius);

    /**
     * 특정 역의 PlaceCard 캐시를 배치로 갱신
     * <p>
     * GEO 캐시에 존재하는 장소 ID 목록을 기반으로
     * PlaceCard 캐시가 없는 장소들을 일괄 API 조회하여 캐싱합니다.
     * </p>
     *
     * @param stationId 역 ID
     * @param longitude 기준 경도
     * @param latitude  기준 위도
     * @return 갱신된 PlaceCard 캐시 수
     */
    int syncPlaceCardsInBatch(Long stationId, double longitude, double latitude);

}
