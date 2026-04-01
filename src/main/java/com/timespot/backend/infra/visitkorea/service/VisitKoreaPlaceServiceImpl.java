package com.timespot.backend.infra.visitkorea.service;

import static com.timespot.backend.common.response.ErrorCode.PLACE_NOT_FOUND;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.CARD_CACHE_KEY_PREFIX;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.DETAIL_CACHE_KEY_PREFIX;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.GEO_KEY_PREFIX;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.PLACE_CARD_CACHE_TTL;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.PLACE_DETAIL_CACHE_TTL;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.mapCategoryToContentTypeId;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.mapContentTypeIdToCategory;
import static com.timespot.backend.infra.visitkorea.model.ContentType.getAllContentTypes;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.infra.redis.dao.RedisGeoRepository;
import com.timespot.backend.infra.redis.dao.RedisRepository;
import com.timespot.backend.infra.redis.model.GeoPlace;
import com.timespot.backend.infra.redis.model.PlaceCardCache;
import com.timespot.backend.infra.redis.model.PlaceDetailCache;
import com.timespot.backend.infra.visitkorea.client.VisitKoreaApiClient;
import com.timespot.backend.infra.visitkorea.client.properties.VisitKoreaProperties;
import com.timespot.backend.infra.visitkorea.dto.VisitKoreaResponseDto.DetailInfoItem;
import com.timespot.backend.infra.visitkorea.dto.VisitKoreaResponseDto.DetailInfoResponse;
import com.timespot.backend.infra.visitkorea.dto.VisitKoreaResponseDto.ImageListItem;
import com.timespot.backend.infra.visitkorea.dto.VisitKoreaResponseDto.ImageListResponse;
import com.timespot.backend.infra.visitkorea.dto.VisitKoreaResponseDto.LocationBasedListItem;
import com.timespot.backend.infra.visitkorea.dto.VisitKoreaResponseDto.LocationBasedListResponse;
import com.timespot.backend.infra.visitkorea.model.ContentType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PackageName : com.timespot.backend.infra.visitkorea.service
 * FileName    : VisitKoreaPlaceServiceImpl
 * Author      : loadingKKamo21
 * Date        : 26. 4. 1.
 * Description : 한국관광공사 API 기반 장소 조회 및 캐싱 서비스 구현체
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 4. 1.     loadingKKamo21       Initial creation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VisitKoreaPlaceServiceImpl implements VisitKoreaPlaceService {

    private final RedisRepository      redisRepository;
    private final RedisGeoRepository   redisGeoRepository;
    private final VisitKoreaApiClient  visitKoreaApiClient;
    private final VisitKoreaProperties visitKoreaProperties;

    @Override
    public Optional<PlaceCardCache> getPlaceCardCache(final String placeId) {
        String cacheKey = CARD_CACHE_KEY_PREFIX + placeId;
        return redisRepository.getValue(cacheKey, PlaceCardCache.class);
    }

    @Override
    public void savePlaceCardCache(final String placeId, final PlaceCardCache cache) {
        String cacheKey = CARD_CACHE_KEY_PREFIX + placeId;
        redisRepository.setValue(cacheKey, cache, PLACE_CARD_CACHE_TTL);
        log.debug("PlaceCardCache 저장: placeId={}, key={}", placeId, cacheKey);
    }

    @Override
    public Optional<PlaceDetailCache> getPlaceDetailCache(final String placeId) {
        String cacheKey = DETAIL_CACHE_KEY_PREFIX + placeId;
        return redisRepository.getValue(cacheKey, PlaceDetailCache.class);
    }

    @Override
    public void savePlaceDetailCache(final String placeId, final PlaceDetailCache cache) {
        String cacheKey = DETAIL_CACHE_KEY_PREFIX + placeId;
        redisRepository.setValue(cacheKey, cache, PLACE_DETAIL_CACHE_TTL);
        log.debug("PlaceDetailCache 저장: placeId={}, key={}", placeId, cacheKey);
    }

    @Override
    public void addPlaceToGeo(final String geoKey,
                              final String placeId,
                              final double longitude,
                              final double latitude) {
        redisGeoRepository.addPlace(geoKey, placeId, longitude, latitude);
        log.debug("GEO 에 장소 추가: geoKey={}, placeId=({}, {})", geoKey, latitude, longitude);
    }

    @Override
    public List<GeoPlace> findPlacesWithinRadius(final String geoKey,
                                                 final double longitude,
                                                 final double latitude,
                                                 final double radiusMeters) {
        return redisGeoRepository.findPlacesWithinRadius(geoKey, longitude, latitude, radiusMeters);
    }

    @Override
    public void deleteGeoKey(final String geoKey) {
        redisGeoRepository.deleteGeoKey(geoKey);
        log.debug("GEO 키 삭제: geoKey={}", geoKey);
    }

    @Override
    @Transactional
    public List<GeoPlace> syncPlacesFromVisitKorea(final Long stationId,
                                                   final double longitude,
                                                   final double latitude,
                                                   final int searchRadius) {
        String geoKey = GEO_KEY_PREFIX + stationId;

        List<GeoPlace> existingPlaces = findPlacesWithinRadius(geoKey, longitude, latitude, searchRadius);
        if (!existingPlaces.isEmpty()) {
            log.info("이미 캐시된 GEO 데이터 존재, 동기화 스킵: stationId={}, count={}", stationId, existingPlaces.size());
            return existingPlaces;
        }

        log.info("GEO 캐시 없음, VisitKorea API 동기화 시작: stationId={}, radius={}", stationId, searchRadius);

        List<GeoPlace>       allPlaces       = new ArrayList<>();
        Set<String>          placeIdSet      = new HashSet<>();
        List<GeoPlace>       geoBatch        = new ArrayList<>();
        List<PlaceCardCache> cardBatch       = new ArrayList<>();
        Set<String>          validCategories = Set.of("관광지", "음식점", "문화시설", "레포츠", "쇼핑");

        for (ContentType contentType : getAllContentTypes()) {
            log.debug("VisitKorea API 요청: stationId={}, contentType={}, pages={}",
                      stationId, contentType, visitKoreaProperties.getSyncPages());

            for (int page = 1; page <= visitKoreaProperties.getSyncPages(); page++) {
                log.debug("VisitKorea API 페이지 요청: page={}", page);

                LocationBasedListResponse response = visitKoreaApiClient.locationBasedList(
                        longitude,
                        latitude,
                        searchRadius,
                        contentType,
                        page,
                        visitKoreaProperties.getPageSize()
                );

                if (!response.isSuccess()) {
                    log.error("VisitKorea API 응답 실패: stationId={}, contentType={}, page={}",
                              stationId, contentType, page);
                    break;
                }

                if (response.getBody().getItems().getItem() == null) {
                    log.debug("VisitKorea API 항목 없음: stationId={}, contentType={}, page={}",
                              stationId, contentType, page);
                    break;
                }

                List<LocationBasedListItem> items = response.getBody().getItems().getItem();
                log.debug("VisitKorea API 항목 수: page={}, count={}", page, items.size());

                for (LocationBasedListItem item : items) {
                    if (item.getMapX() == null || item.getMapY() == null) {
                        log.debug("좌표 없음: contentId={}", item.getContentId());
                        continue;
                    }

                    if (item.getDist() != null && item.getDist() > searchRadius) {
                        log.debug("반경 초과: contentId={}, dist={}", item.getContentId(), item.getDist());
                        continue;
                    }

                    String placeId = item.getContentId();

                    if (!placeIdSet.add(placeId)) {
                        log.debug("중복 장소: contentId={}", placeId);
                        continue;
                    }

                    String category = mapContentTypeIdToCategory(item.getContentTypeId());
                    if (!validCategories.contains(category)) {
                        log.debug("유효하지 않은 카테고리 스킵: placeId={}, contentTypeId={}, category={}",
                                  placeId, item.getContentTypeId(), category);
                        continue;
                    }

                    geoBatch.add(GeoPlace.builder()
                                         .placeId(placeId)
                                         .latitude(item.getMapY())
                                         .longitude(item.getMapX())
                                         .distance(item.getDist() != null ? item.getDist() : 0.0)
                                         .build());

                    cardBatch.add(new PlaceCardCache(
                            placeId,
                            item.getName(),
                            category,
                            item.getFullAddress(),
                            item.getMapY(),
                            item.getMapX(),
                            item.getDist() != null ? item.getDist() : 0.0,
                            item.getFirstImage()
                    ));

                    allPlaces.add(GeoPlace.builder()
                                          .placeId(placeId)
                                          .latitude(item.getMapY())
                                          .longitude(item.getMapX())
                                          .distance(item.getDist() != null ? item.getDist() : 0.0)
                                          .build());
                }

                if (items.size() < visitKoreaProperties.getPageSize()) {
                    log.info("마지막 페이지 도달: page={}, itemCount={}", page, items.size());
                    break;
                }
            }
        }

        log.info("GEO 배치 저장 시작: count={}", geoBatch.size());
        for (GeoPlace place : geoBatch)
            redisGeoRepository.addPlace(geoKey, place.getPlaceId(), place.getLongitude(), place.getLatitude());

        log.info("PlaceCard 배치 저장 시작: count={}", cardBatch.size());
        for (PlaceCardCache cache : cardBatch)
            savePlaceCardCache(cache.getPlaceId(), cache);

        log.info("VisitKorea 동기화 완료: stationId={}, totalPlaces={}, filteredPlaces={}",
                 stationId, allPlaces.size(), placeIdSet.size());

        return allPlaces;
    }

    @Override
    public PlaceDetailCache getPlaceDetailWithFallback(final String placeId) {
        return getPlaceDetailCache(placeId).orElseGet(() -> fetchAndCachePlaceDetail(placeId));
    }

    @Override
    public PlaceCardCache getPlaceCardWithFallback(final String placeId) {
        return getPlaceCardCache(placeId).orElseGet(() -> fetchAndCachePlaceCard(placeId));
    }

    // ========================= 내부 메서드 =========================

    /**
     * 장소 카드 정보를 API 에서 조회하여 캐싱
     *
     * @param placeId 장소 ID
     * @return 장소 카드 캐시
     */
    private PlaceCardCache fetchAndCachePlaceCard(final String placeId) {
        log.info("캐시가 없어 VisitKorea API 에서 장소 카드 정보 조회: placeId={}", placeId);

        LocationBasedListResponse response = visitKoreaApiClient.locationBasedListForSinglePlace(placeId);

        if (!response.isSuccess()
            || response.getBody().getItems().getItem() == null
            || response.getBody().getItems().getItem().isEmpty()) {
            log.error("장소 정보 조회 실패: placeId={}", placeId);
            throw new GlobalException(PLACE_NOT_FOUND);
        }

        LocationBasedListItem item = response.getBody().getItems().getItem().get(0);

        if (item.getMapX() == null || item.getMapY() == null) {
            log.error("좌표 정보 없음: placeId={}", placeId);
            throw new GlobalException(PLACE_NOT_FOUND);
        }

        String category = mapContentTypeIdToCategory(item.getContentTypeId());

        PlaceCardCache cardCache = new PlaceCardCache(
                placeId,
                item.getName(),
                category,
                item.getFullAddress(),
                item.getMapY(),
                item.getMapX(),
                item.getDist() != null ? item.getDist() : 0.0,
                item.getFirstImage()
        );

        savePlaceCardCache(placeId, cardCache);

        log.info("장소 카드 정보 캐시 저장 완료: placeId={}", placeId);

        return cardCache;
    }

    /**
     * 장소 상세 정보를 API 에서 조회하여 캐싱
     *
     * @param placeId 장소 ID
     * @return 장소 상세 캐시
     */
    private PlaceDetailCache fetchAndCachePlaceDetail(final String placeId) {
        log.info("캐시가 없어 VisitKorea API 에서 장소 상세 정보 조회: placeId={}", placeId);

        PlaceCardCache cardCache = getPlaceCardCache(placeId).orElseGet(() -> fetchAndCachePlaceCard(placeId));

        ContentType contentType = ContentType.from(mapCategoryToContentTypeId(cardCache.getCategory()));

        DetailInfoResponse detailResponse = visitKoreaApiClient.detailIntro(placeId, contentType);

        List<String> images = fetchPlaceImages(placeId);

        PlaceDetailCache detailCache = buildPlaceDetailCache(cardCache, detailResponse, images);

        savePlaceDetailCache(placeId, detailCache);

        log.info("장소 상세 정보 캐시 저장 완료: placeId={}", placeId);

        return detailCache;
    }

    /**
     * 장소 이미지 목록 조회
     *
     * @param placeId 장소 ID
     * @return 이미지 URL 목록
     */
    private List<String> fetchPlaceImages(final String placeId) {
        try {
            ImageListResponse response = visitKoreaApiClient.detailImage(placeId, 1, 10);
            if (response.isSuccess() && response.getBody().getItems() != null)
                return response.getBody().getItems().getItem().stream()
                               .map(ImageListItem::getOriginImgUrl)
                               .filter(url -> url != null && !url.isBlank())
                               .toList();
        } catch (Exception e) {
            log.warn("이미지 조회 실패: placeId={}, error={}", placeId, e.getMessage());
        }
        return List.of();
    }

    /**
     * PlaceDetailCache 빌드
     *
     * @param cardCache      장소 카드 캐시
     * @param detailResponse 상세 정보 API 응답
     * @param images         이미지 URL 목록
     * @return 장소 상세 캐시
     */
    private PlaceDetailCache buildPlaceDetailCache(final PlaceCardCache cardCache,
                                                   final DetailInfoResponse detailResponse,
                                                   final List<String> images) {
        if (!detailResponse.isSuccess() || detailResponse.getBody().getItems() == null) {
            log.warn("상세 정보 API 응답 없음: placeId={}", cardCache.getPlaceId());
            return PlaceDetailCache.empty();
        }

        DetailInfoItem item = detailResponse.getBody().getItems().getItem().get(0);

        return PlaceDetailCache.builder()
                               .placeId(cardCache.getPlaceId())
                               .contentTypeId(mapCategoryToContentTypeId(cardCache.getCategory()))
                               .phoneNumber(item.getInfoCenter())
                               .images(images)
                               .restDate(item.getRestDate())
                               .useTime(item.getUseTime())
                               .scale(item.getScale())
                               // 관광지 (12)
                               .openDate(item.getOpenDate())
                               .useSeason(item.getUseSeason())
                               .expAgeRange(item.getExpAgeRange())
                               .expGuide(item.getExpGuide())
                               .heritage1(item.getHeritage1())
                               .accomCount(item.getAccomCount())
                               .chkBabyCarriage(item.getChkBabyCarriage())
                               .chkPet(item.getChkPet())
                               // 음식점 (39)
                               .firstMenu(item.getFirstMenu())
                               .treatMenu(item.getTreatMenu())
                               .seat(item.getSeat())
                               .smoking(item.getSmoking())
                               .packing(item.getPacking())
                               .kidsFacility(item.getKidsFacility())
                               .openDateFood(item.getOpenDateFood())
                               // 문화시설 (14)
                               .spendTime(item.getSpendTime())
                               .useFee(item.getUseFee())
                               .discountInfo(item.getDiscountInfo())
                               .accomCountCulture(item.getAccomCountCulture())
                               .parkingCulture(item.getParkingCulture())
                               // 레포츠 (28)
                               .openPeriod(item.getOpenPeriod())
                               .useFeeLeports(item.getUseFeeLeports())
                               .reservation(item.getReservation())
                               .scaleLeports(item.getScaleLeports())
                               .expAgeRangeLeports(item.getExpAgeRangeLeports())
                               // 쇼핑 (38)
                               .openTime(item.getOpenTime())
                               .saleItem(item.getSaleItem())
                               .shopGuide(item.getShopGuide())
                               .scaleShopping(item.getScaleShopping())
                               .fairDay(item.getFairDay())
                               .build();
    }

}
