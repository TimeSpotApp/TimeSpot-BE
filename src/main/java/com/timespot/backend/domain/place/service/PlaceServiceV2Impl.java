package com.timespot.backend.domain.place.service;

import static com.timespot.backend.common.response.ErrorCode.PLACE_NOT_FOUND;
import static com.timespot.backend.common.response.ErrorCode.STATION_NOT_FOUND;
import static com.timespot.backend.domain.place.constant.PlaceConst.MINIMUM_STAY_TIME;
import static com.timespot.backend.domain.place.constant.PlaceConst.PLATFORM_WAIT_TIME;
import static com.timespot.backend.domain.place.constant.PlaceConst.WALK_SPEED_PER_MINUTE;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.domain.place.dto.PlaceResponseDtoV2.AvailablePlace;
import com.timespot.backend.domain.place.dto.PlaceResponseDtoV2.CulturePlaceDetail;
import com.timespot.backend.domain.place.dto.PlaceResponseDtoV2.PlaceDetail;
import com.timespot.backend.domain.place.dto.PlaceResponseDtoV2.RestaurantDetail;
import com.timespot.backend.domain.place.dto.PlaceResponseDtoV2.ShoppingPlaceDetail;
import com.timespot.backend.domain.place.dto.PlaceResponseDtoV2.SportsPlaceDetail;
import com.timespot.backend.domain.place.dto.PlaceResponseDtoV2.TouristPlaceDetail;
import com.timespot.backend.domain.place.model.PlaceCategory;
import com.timespot.backend.domain.station.dao.StationRepository;
import com.timespot.backend.domain.station.model.Station;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PackageName : com.timespot.backend.domain.place.service
 * FileName    : PlaceServiceV2Impl
 * Author      : loadingKKamo21
 * Date        : 26. 3. 29.
 * Description : Place API V2 서비스 구현체
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 29.    loadingKKamo21       Initial creation
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PlaceServiceV2Impl implements PlaceServiceV2 {

    private static final Duration PLACE_CACHE_TTL  = Duration.ofHours(24);
    private static final Duration DETAIL_CACHE_TTL = Duration.ofDays(7);

    private final StationRepository    stationRepository;
    private final RedisRepository      redisRepository;
    private final RedisGeoRepository   redisGeoRepository;
    private final VisitKoreaApiClient  visitKoreaApiClient;
    private final VisitKoreaProperties visitKoreaProperties;

    @Override
    public Page<AvailablePlace> findAvailablePlaces(
            final Long stationId,
            final double userLat,
            final double userLon,
            final Double mapLat,
            final Double mapLon,
            final int remainingMinutes,
            final String keyword,
            final String category,
            final Pageable pageable
    ) {
        log.info("방문 가능 장소 조회: stationId={}, keyword={}, category={}, pageable={}",
                 stationId, keyword, category, pageable);

        Station station = validateStation(stationId);

        int searchRadius = visitKoreaProperties.getMaxRadiusMeters();

        List<GeoPlace> geoPlaces = getPlacesFromGeoOrApi(station, searchRadius);

        if (geoPlaces.isEmpty()) {
            log.info("GEO 에 데이터가 없어 VisitKorea API 동기화 수행: stationId={}", station.getId());
            List<GeoPlace> syncedPlaces = syncFromVisitKorea(station, searchRadius);
            log.info("동기화 완료 후 반환받은 장소 수: stationId={}, count={}", station.getId(), syncedPlaces.size());

            geoPlaces = syncedPlaces;
            log.info("동기화된 GEO 데이터 사용: stationId={}, count={}", station.getId(), geoPlaces.size());
        }

        log.info("GEO 장소 필터링 시작: totalCount={}, keyword={}, category={}",
                 geoPlaces.size(), keyword, category);

        List<GeoPlace> filtered = filterPlaces(geoPlaces, keyword, category);

        log.info("GEO 장소 필터링 완료: filteredCount={}", filtered.size());

        List<GeoPlace> sorted = sortPlaces(
                filtered, pageable.getSort(), userLat, userLon, mapLat, mapLon
        );

        log.info("GEO 장소 정렬 완료: sortedCount={}", sorted.size());

        Page<AvailablePlace> result = buildAvailablePlacePage(sorted, pageable, userLat, userLon, remainingMinutes);

        log.info("방문 가능 장소 조회 완료: totalElements={}, page={}, size={}, totalPages={}",
                 result.getTotalElements(), result.getNumber(), result.getSize(), result.getTotalPages());

        return result;
    }

    @Override
    public PlaceDetail getPlaceDetail(
            final String placeId,
            final Long stationId,
            final double userLat,
            final double userLon,
            final int remainingMinutes
    ) {
        log.info("장소 상세 정보 조회: placeId={}, stationId={}, remainingMinutes={}",
                 placeId, stationId, remainingMinutes);

        PlaceCardCache cardInfo = getPlaceCardCache(placeId).orElseThrow(() -> new GlobalException(PLACE_NOT_FOUND));

        PlaceDetailCache detailInfo = getPlaceDetailCache(placeId)
                .orElseGet(() -> fetchAndCachePlaceDetail(placeId, cardInfo));

        Station station = validateStation(stationId);

        double distanceFromStation = calculateDistance(
                station.getLatitude(), station.getLongitude(),
                cardInfo.getLatitude(), cardInfo.getLongitude()
        );
        int walkTimeFromStation = calculateWalkTime(distanceFromStation);

        int           stayableMinutes = calculateStayableMinutes(remainingMinutes, walkTimeFromStation);
        LocalDateTime leaveTime       = LocalDateTime.now().plusMinutes(remainingMinutes - walkTimeFromStation);

        return buildTypedPlaceDetail(
                cardInfo, detailInfo, station, distanceFromStation, walkTimeFromStation, stayableMinutes, leaveTime
        );
    }

    // ========================= 내부 메서드 =========================

    /**
     * 역 정보 조회 및 검증
     */
    private Station validateStation(final Long stationId) {
        return stationRepository.findById(stationId).orElseThrow(() -> new GlobalException(STATION_NOT_FOUND));
    }

    /**
     * GEO 에서 장소 조회
     */
    private List<GeoPlace> getPlacesFromGeoOrApi(final Station station, final int searchRadius) {
        String geoKey = "place:geo:" + station.getId();
        List<GeoPlace> geoPlaces = redisGeoRepository.findPlacesWithinRadius(
                geoKey,
                station.getLongitude(),
                station.getLatitude(),
                searchRadius
        );
        log.info("GEO 검색 결과: stationId={}, count={}", station.getId(), geoPlaces.size());

        return geoPlaces;
    }

    /**
     * VisitKorea API 에서 장소 동기화
     */
    private List<GeoPlace> syncFromVisitKorea(final Station station, final int searchRadius) {
        String geoKey = "place:geo:" + station.getId();

        log.info("GEO 캐시 확인 시작: key={}, stationId={}", geoKey, station.getId());
        List<GeoPlace> existingPlaces = redisGeoRepository.findPlacesWithinRadius(
                geoKey, station.getLongitude(), station.getLatitude(), searchRadius
        );
        log.info("GEO 캐시 확인 결과: stationId={}, count={}", station.getId(), existingPlaces.size());

        if (!existingPlaces.isEmpty()) {
            log.info("이미 캐시된 GEO 데이터 존재, 동기화 스킵: stationId={}, count={}", station.getId(), existingPlaces.size());
            return existingPlaces;
        }
        log.info("GEO 캐시 없음, VisitKorea API 동기화 시작: stationId={}, radius={}", station.getId(), searchRadius);

        List<GeoPlace> allPlaces  = new ArrayList<>();
        Set<String>    placeIdSet = new HashSet<>();

        List<GeoPlace>       geoBatch  = new ArrayList<>();
        List<PlaceCardCache> cardBatch = new ArrayList<>();

        Set<String> validCategories = Set.of("관광지", "음식점", "문화시설", "레포츠", "쇼핑");

        for (ContentType contentType : ContentType.getAllContentTypes()) {
            log.debug("VisitKorea API 요청: stationId={}, contentType={}, pages={}",
                      station.getId(), contentType, visitKoreaProperties.getSyncPages());

            for (int page = 1; page <= visitKoreaProperties.getSyncPages(); page++) {
                log.debug("VisitKorea API 페이지 요청: page={}", page);

                LocationBasedListResponse response = visitKoreaApiClient.locationBasedList(
                        station.getLongitude(),
                        station.getLatitude(),
                        searchRadius,
                        contentType,
                        page,
                        visitKoreaProperties.getPageSize()
                );

                if (!response.isSuccess()) {
                    log.error("VisitKorea API 응답 실패: stationId={}, contentType={}, page={}",
                              station.getId(), contentType, page);
                    break;
                }

                if (response.getBody().getItems().getItem() == null) {
                    log.debug("VisitKorea API 항목 없음: stationId={}, contentType={}, page={}",
                              station.getId(), contentType, page);
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
                 station.getId(), allPlaces.size(), placeIdSet.size());

        return allPlaces;
    }

    /**
     * 장소 필터링 (키워드, 카테고리)
     * <p>
     * 클라이언트 요청 카테고리 (etc, shopping, activity, restaurant, cafe) 를
     * 서버 내부 카테고리 (관광지, 문화시설, 레포츠, 쇼핑, 음식점) 로 매핑하여 필터링합니다.
     * </p>
     * <p>
     * - ETC: 문화시설 + 관광지<br>
     * - SHOPPING: 쇼핑<br>
     * - ACTIVITY: 레포츠<br>
     * - RESTAURANT: 음식점 (카페 키워드 제외)<br>
     * - CAFE: 음식점 중 '카페' 키워드 포함
     * </p>
     */
    private List<GeoPlace> filterPlaces(final List<GeoPlace> places,
                                        final String keyword,
                                        final String category) {
        // 카테고리가 null 이나 "전체"이면 필터링 안 함
        if (category == null || category.isBlank() || "전체".equals(category)) {
            return filterByKeywordOnly(places, keyword);
        }

        // 클라이언트 요청 카테고리를 PlaceCategory 로 변환
        PlaceCategory placeCategory;
        try {
            placeCategory = PlaceCategory.from(category);
        } catch (GlobalException e) {
            log.warn("지원하지 않는 카테고리: category={}", category);
            return filterByKeywordOnly(places, keyword);
        }

        log.debug("카테고리 필터링 적용: category={}, serverCategories={}",
                  placeCategory.name(), placeCategory.getServerCategories());

        return places.stream()
                     // 키워드 필터링 (먼저 적용)
                     .filter(place -> matchesKeyword(place, keyword))
                     // 카테고리 필터링
                     .filter(place -> matchesCategory(place, placeCategory))
                     // 카페/레스토랑 키워드 필터링 (필요한 경우)
                     .filter(place -> matchesCafeKeyword(place, placeCategory))
                     .toList();
    }

    /**
     * 카테고리 미지정 시 키워드만 필터링
     */
    private List<GeoPlace> filterByKeywordOnly(final List<GeoPlace> places, final String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return places;
        }
        log.debug("키워드 필터링만 수행: keyword={}", keyword);
        return places.stream()
                     .filter(place -> matchesKeyword(place, keyword))
                     .toList();
    }

    /**
     * 키워드 매칭 확인 (장소명 또는 카테고리)
     */
    private boolean matchesKeyword(final GeoPlace place, final String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        PlaceCardCache cache = getPlaceCardCache(place.getPlaceId()).orElse(null);
        if (cache == null) return false;
        return cache.getName().contains(keyword) || cache.getCategory().contains(keyword);
    }

    /**
     * 서버 카테고리 매칭 확인
     */
    private boolean matchesCategory(final GeoPlace place, final PlaceCategory placeCategory) {
        PlaceCardCache cache = getPlaceCardCache(place.getPlaceId()).orElse(null);
        if (cache == null) return false;

        // CAFE 는 음식점 카테고리에서 검색하므로 별도 처리
        if (placeCategory == PlaceCategory.CAFE) {
            return "음식점".equals(cache.getCategory());
        }

        return placeCategory.getServerCategories().contains(cache.getCategory());
    }

    /**
     * 카페 키워드 필터링
     * - RESTAURANT: '카페' 키워드 제외
     * - CAFE: '카페' 키워드 포함만
     */
    private boolean matchesCafeKeyword(final GeoPlace place, final PlaceCategory placeCategory) {
        if (!placeCategory.requiresCafeKeywordFilter()) {
            return true; // 필터링 불필요
        }

        PlaceCardCache cache = getPlaceCardCache(place.getPlaceId()).orElse(null);
        if (cache == null) return false;

        String name     = cache.getName();
        String category = cache.getCategory();

        boolean isCafeCategory   = "카페".equals(category);
        boolean nameContainsCafe = name != null && name.contains("카페");

        if (placeCategory.isCafeOnly()) {
            // CAFE: '카페' 키워드 포함만
            return isCafeCategory || nameContainsCafe;
        } else {
            // RESTAURANT: '카페' 키워드 제외
            return !isCafeCategory && !nameContainsCafe;
        }
    }

    /**
     * 장소 정렬 (Pageable 의 Sort 사용)
     */
    private List<GeoPlace> sortPlaces(final List<GeoPlace> places,
                                      final Sort sort,
                                      final double userLat,
                                      final double userLon,
                                      final Double mapLat,
                                      final Double mapLon) {
        if (sort.isUnsorted())
            return places.stream().sorted(Comparator.comparingDouble(GeoPlace::getDistance)).toList();

        for (Sort.Order order : sort) {
            String property = order.getProperty();

            if ("distanceFromUser".equals(property))
                return places.stream()
                             .sorted(Comparator.comparingDouble(
                                     place -> calculateDistance(userLat,
                                                                userLon,
                                                                place.getLatitude(),
                                                                place.getLongitude())
                             ))
                             .toList();

            if ("distanceFromCenter".equals(property) && mapLat != null && mapLon != null)
                return places.stream()
                             .sorted(Comparator.comparingDouble(
                                     place -> calculateDistance(mapLat,
                                                                mapLon,
                                                                place.getLatitude(),
                                                                place.getLongitude())
                             ))
                             .toList();
        }

        return places.stream()
                     .sorted(Comparator.comparingDouble(GeoPlace::getDistance))
                     .toList();
    }

    /**
     * AvailablePlace 페이지 빌드
     */
    private Page<AvailablePlace> buildAvailablePlacePage(
            final List<GeoPlace> places,
            final Pageable pageable,
            final double userLat,
            final double userLon,
            final int remainingMinutes
    ) {
        int start = (int) pageable.getOffset();

        List<AvailablePlace> content = places.stream()
                                             .skip(start)
                                             .limit(pageable.getPageSize())
                                             .map(place -> {
                                                 try {
                                                     return buildAvailablePlace(
                                                             place, userLat, userLon, remainingMinutes
                                                     );
                                                 } catch (Exception e) {
                                                     log.warn("AvailablePlace 빌드 실패: placeId={}, error={}",
                                                              place.getPlaceId(), e.getMessage());
                                                     return null;
                                                 }
                                             })
                                             .filter(Objects::nonNull)
                                             .toList();

        return new PageImpl<>(content, pageable, places.size());
    }

    /**
     * AvailablePlace 빌드 (생성자 사용)
     */
    private AvailablePlace buildAvailablePlace(
            final GeoPlace geoPlace,
            final double userLat,
            final double userLon,
            final int remainingMinutes
    ) {
        log.debug("AvailablePlace 빌드 시작: placeId={}", geoPlace.getPlaceId());

        PlaceCardCache cardInfo = getPlaceCardCache(geoPlace.getPlaceId())
                .orElseThrow(() -> {
                    log.error("PlaceCardCache 없음: placeId={}", geoPlace.getPlaceId());
                    return new GlobalException(PLACE_NOT_FOUND);
                });

        log.debug("PlaceCardCache 조회 성공: placeId={}, name={}", geoPlace.getPlaceId(), cardInfo.getName());

        double distanceFromUser = calculateDistance(
                userLat, userLon, geoPlace.getLatitude(), geoPlace.getLongitude()
        );

        int walkTimeFromStation = calculateWalkTime(geoPlace.getDistance());
        int stayableMinutes     = calculateStayableMinutes(remainingMinutes, walkTimeFromStation);

        boolean visitable = stayableMinutes >= MINIMUM_STAY_TIME;

        log.debug("AvailablePlace 빌드 완료: placeId={}, visitable={}, stayableMinutes={}",
                  geoPlace.getPlaceId(), visitable, stayableMinutes);

        return new AvailablePlace(
                geoPlace.getPlaceId(),
                cardInfo.getName(),
                cardInfo.getCategory(),
                cardInfo.getAddress(),
                geoPlace.getLatitude(),
                geoPlace.getLongitude(),
                distanceFromUser,
                geoPlace.getDistance(),
                walkTimeFromStation,
                stayableMinutes,
                visitable,
                cardInfo.getImageUrl()
        );
    }

    /**
     * 타입별 PlaceDetail 생성
     */
    private PlaceDetail buildTypedPlaceDetail(
            final PlaceCardCache cardInfo,
            final PlaceDetailCache detailInfo,
            final Station station,
            final double distanceFromStation,
            final int walkTimeFromStation,
            final int stayableMinutes,
            final LocalDateTime leaveTime
    ) {
        String category = cardInfo.getCategory();

        boolean visitable = stayableMinutes >= MINIMUM_STAY_TIME;

        return switch (category) {
            case "관광지" -> buildTouristPlaceDetail(
                    cardInfo, detailInfo, station, distanceFromStation, walkTimeFromStation, stayableMinutes, visitable,
                    leaveTime
            );
            case "음식점" -> buildRestaurantDetail(
                    cardInfo, detailInfo, station, distanceFromStation, walkTimeFromStation, stayableMinutes, visitable,
                    leaveTime
            );
            case "문화시설" -> buildCulturePlaceDetail(
                    cardInfo, detailInfo, station, distanceFromStation, walkTimeFromStation, stayableMinutes, visitable,
                    leaveTime
            );
            case "레포츠" -> buildSportsPlaceDetail(
                    cardInfo, detailInfo, station, distanceFromStation, walkTimeFromStation, stayableMinutes, visitable,
                    leaveTime
            );
            case "쇼핑" -> buildShoppingPlaceDetail(
                    cardInfo, detailInfo, station, distanceFromStation, walkTimeFromStation, stayableMinutes, visitable,
                    leaveTime
            );
            default -> buildTouristPlaceDetail(
                    cardInfo, detailInfo, station, distanceFromStation, walkTimeFromStation, stayableMinutes, visitable,
                    leaveTime
            );
        };
    }

    /**
     * 관광지 상세 정보 빌드
     */
    private TouristPlaceDetail buildTouristPlaceDetail(
            final PlaceCardCache cardInfo,
            final PlaceDetailCache detailInfo,
            final Station station,
            final double distanceFromStation,
            final int walkTimeFromStation,
            final int stayableMinutes,
            final boolean visitable,
            final LocalDateTime leaveTime
    ) {
        return new TouristPlaceDetail(
                cardInfo.getPlaceId(),
                cardInfo.getName(),
                cardInfo.getCategory(),
                cardInfo.getAddress(),
                cardInfo.getLatitude(),
                cardInfo.getLongitude(),
                (int) distanceFromStation,
                walkTimeFromStation,
                stayableMinutes,
                visitable,
                station.getLatitude(),
                station.getLongitude(),
                leaveTime,
                detailInfo.getImages(),
                detailInfo.getPhoneNumber(),
                detailInfo.getRestDate(),
                detailInfo.getUseTime(),
                detailInfo.getOpenDate(),
                detailInfo.getUseSeason(),
                detailInfo.getExpAgeRange(),
                detailInfo.getExpGuide(),
                detailInfo.getHeritage1(),
                detailInfo.getAccomCount(),
                detailInfo.getChkBabyCarriage(),
                detailInfo.getChkPet()
        );
    }

    /**
     * 음식점 상세 정보 빌드
     */
    private RestaurantDetail buildRestaurantDetail(
            final PlaceCardCache cardInfo,
            final PlaceDetailCache detailInfo,
            final Station station,
            final double distanceFromStation,
            final int walkTimeFromStation,
            final int stayableMinutes,
            final boolean visitable,
            final LocalDateTime leaveTime
    ) {
        return new RestaurantDetail(
                cardInfo.getPlaceId(),
                cardInfo.getName(),
                cardInfo.getCategory(),
                cardInfo.getAddress(),
                cardInfo.getLatitude(),
                cardInfo.getLongitude(),
                (int) distanceFromStation,
                walkTimeFromStation,
                stayableMinutes,
                visitable,
                station.getLatitude(),
                station.getLongitude(),
                leaveTime,
                detailInfo.getImages(),
                detailInfo.getPhoneNumber(),
                detailInfo.getRestDate(),
                detailInfo.getUseTime(),
                detailInfo.getFirstMenu(),
                detailInfo.getTreatMenu(),
                detailInfo.getSeat(),
                detailInfo.getSmoking(),
                detailInfo.getPacking(),
                detailInfo.getKidsFacility(),
                detailInfo.getOpenDateFood()
        );
    }

    /**
     * 문화시설 상세 정보 빌드
     */
    private CulturePlaceDetail buildCulturePlaceDetail(
            final PlaceCardCache cardInfo,
            final PlaceDetailCache detailInfo,
            final Station station,
            final double distanceFromStation,
            final int walkTimeFromStation,
            final int stayableMinutes,
            final boolean visitable,
            final LocalDateTime leaveTime
    ) {
        return new CulturePlaceDetail(
                cardInfo.getPlaceId(),
                cardInfo.getName(),
                cardInfo.getCategory(),
                cardInfo.getAddress(),
                cardInfo.getLatitude(),
                cardInfo.getLongitude(),
                (int) distanceFromStation,
                walkTimeFromStation,
                stayableMinutes,
                visitable,
                station.getLatitude(),
                station.getLongitude(),
                leaveTime,
                detailInfo.getImages(),
                detailInfo.getPhoneNumber(),
                detailInfo.getRestDate(),
                detailInfo.getUseTime(),
                detailInfo.getSpendTime(),
                detailInfo.getUseFee(),
                detailInfo.getDiscountInfo(),
                detailInfo.getAccomCountCulture(),
                detailInfo.getParkingCulture()
        );
    }

    /**
     * 레포츠 상세 정보 빌드
     */
    private SportsPlaceDetail buildSportsPlaceDetail(
            final PlaceCardCache cardInfo,
            final PlaceDetailCache detailInfo,
            final Station station,
            final double distanceFromStation,
            final int walkTimeFromStation,
            final int stayableMinutes,
            final boolean visitable,
            final LocalDateTime leaveTime
    ) {
        return new SportsPlaceDetail(
                cardInfo.getPlaceId(),
                cardInfo.getName(),
                cardInfo.getCategory(),
                cardInfo.getAddress(),
                cardInfo.getLatitude(),
                cardInfo.getLongitude(),
                (int) distanceFromStation,
                walkTimeFromStation,
                stayableMinutes,
                visitable,
                station.getLatitude(),
                station.getLongitude(),
                leaveTime,
                detailInfo.getImages(),
                detailInfo.getPhoneNumber(),
                detailInfo.getRestDate(),
                detailInfo.getUseTime(),
                detailInfo.getOpenPeriod(),
                detailInfo.getUseFeeLeports(),
                detailInfo.getReservation(),
                detailInfo.getScaleLeports(),
                detailInfo.getExpAgeRangeLeports()
        );
    }

    /**
     * 쇼핑 상세 정보 빌드
     */
    private ShoppingPlaceDetail buildShoppingPlaceDetail(
            final PlaceCardCache cardInfo,
            final PlaceDetailCache detailInfo,
            final Station station,
            final double distanceFromStation,
            final int walkTimeFromStation,
            final int stayableMinutes,
            final boolean visitable,
            final LocalDateTime leaveTime
    ) {
        return new ShoppingPlaceDetail(
                cardInfo.getPlaceId(),
                cardInfo.getName(),
                cardInfo.getCategory(),
                cardInfo.getAddress(),
                cardInfo.getLatitude(),
                cardInfo.getLongitude(),
                (int) distanceFromStation,
                walkTimeFromStation,
                stayableMinutes,
                visitable,
                station.getLatitude(),
                station.getLongitude(),
                leaveTime,
                detailInfo.getImages(),
                detailInfo.getPhoneNumber(),
                detailInfo.getRestDate(),
                detailInfo.getUseTime(),
                detailInfo.getOpenTime(),
                detailInfo.getSaleItem(),
                detailInfo.getShopGuide(),
                detailInfo.getScaleShopping(),
                detailInfo.getFairDay()
        );
    }

    /**
     * 체류 가능 시간 계산
     * 왕복 도보 시간을 제외한 나머지 시간
     * 플랫폼 대기 + 최소 체류 고려
     */
    private int calculateStayableMinutes(final int remainingMinutes, final int walkTimeOneWay) {
        // 왕복 도보 시간 = 편도 * 2
        int totalWalkTime = walkTimeOneWay * 2;

        // 체류 가능 시간 = 남은 시간 - 왕복 도보 시간 - 플랫폼 대기 시간 (10 분)
        int stayable = remainingMinutes - totalWalkTime - PLATFORM_WAIT_TIME;

        return Math.max(0, stayable);
    }

    /**
     * PlaceCardCache 조회
     */
    private Optional<PlaceCardCache> getPlaceCardCache(final String placeId) {
        String key = "place:card:" + placeId;
        log.debug("PlaceCardCache 조회: key={}", key);

        Optional<PlaceCardCache> result = redisRepository.getValue(key, PlaceCardCache.class);

        if (result.isPresent())
            log.debug("PlaceCardCache 조회 성공: key={}, name={}", key, result.get().getName());
        else
            log.warn("PlaceCardCache 조회 실패: key={}", key);

        return result;
    }

    /**
     * PlaceCardCache 저장
     */
    private void savePlaceCardCache(final String placeId, final PlaceCardCache cache) {
        String key = "place:card:" + placeId;
        redisRepository.setValue(key, cache, PLACE_CACHE_TTL);
    }

    /**
     * PlaceDetailCache 조회
     */
    private Optional<PlaceDetailCache> getPlaceDetailCache(final String placeId) {
        String key = "place:detail:" + placeId;
        return redisRepository.getValue(key, PlaceDetailCache.class);
    }

    /**
     * 장소 상세 정보 조회 및 캐싱
     */
    private PlaceDetailCache fetchAndCachePlaceDetail(
            final String placeId,
            final PlaceCardCache cardInfo
    ) {
        log.debug("장소 상세 정보 조회: placeId={}", placeId);

        String contentTypeId = extractContentTypeId(cardInfo.getCategory());

        // 상세 정보 조회
        DetailInfoResponse detailResponse = visitKoreaApiClient.detailIntro(
                placeId, ContentType.from(contentTypeId)
        );

        // 공통 필드 초기화
        String infoCenter = null;
        String restDate   = null;
        String useTime    = null;
        String scale      = null;

        // 타입별 필드 초기화
        String openDate          = null, useSeason = null, expAgeRange = null, expGuide = null;
        String heritage1         = null, accomCount = null, chkBabyCarriage = null, chkPet = null;
        String firstMenu         = null, treatMenu = null, seat = null, smoking = null;
        String packing           = null, kidsFacility = null, openDateFood = null;
        String spendTime         = null, useFee = null, discountInfo = null;
        String accomCountCulture = null, parkingCulture = null;
        String openPeriod        = null, useFeeLeports = null, reservation = null;
        String scaleLeports      = null, expAgeRangeLeports = null;
        String openTime          = null, saleItem = null, shopGuide = null;
        String scaleShopping     = null, fairDay = null;

        if (detailResponse.isSuccess() && detailResponse.getBody().getItems().getItem() != null &&
            !detailResponse.getBody().getItems().getItem().isEmpty()) {
            DetailInfoItem detail = detailResponse.getBody().getItems().getItem().get(0);

            // 공통 필드
            infoCenter = detail.getInfoCenter();
            restDate = detail.getRestDate();
            scale = detail.getScale();

            // 타입별 필드 추출
            openDate = detail.getOpenDate();
            useSeason = detail.getUseSeason();
            expAgeRange = detail.getExpAgeRange();
            expGuide = detail.getExpGuide();
            heritage1 = detail.getHeritage1();
            accomCount = detail.getAccomCount();
            chkBabyCarriage = detail.getChkBabyCarriage();
            chkPet = detail.getChkPet();

            firstMenu = detail.getFirstMenu();
            treatMenu = detail.getTreatMenu();
            seat = detail.getSeat();
            smoking = detail.getSmoking();
            packing = detail.getPacking();
            kidsFacility = detail.getKidsFacility();
            openDateFood = detail.getOpenDateFood();

            spendTime = detail.getSpendTime();
            useFee = detail.getUseFee();
            discountInfo = detail.getDiscountInfo();
            accomCountCulture = detail.getAccomCountCulture();
            parkingCulture = detail.getParkingCulture();

            openPeriod = detail.getOpenPeriod();
            useFeeLeports = detail.getUseFeeLeports();
            reservation = detail.getReservation();
            scaleLeports = detail.getScaleLeports();
            expAgeRangeLeports = detail.getExpAgeRangeLeports();

            openTime = detail.getOpenTime();
            saleItem = detail.getSaleItem();
            shopGuide = detail.getShopGuide();
            scaleShopping = detail.getScaleShopping();
            fairDay = detail.getFairDay();

            // 이용시간은 타입별로 필드가 다름
            useTime = getUseTimeByContentType(detail, contentTypeId);
        }

        List<String> images = new ArrayList<>();
        if (cardInfo.getImageUrl() != null && !cardInfo.getImageUrl().isEmpty()) {
            images.add(cardInfo.getImageUrl());
        }

        try {
            ImageListResponse imageResponse = visitKoreaApiClient.detailImage(placeId, 1, 20);

            if (imageResponse.isSuccess() && imageResponse.getBody().getItems().getItem() != null)
                imageResponse.getBody().getItems().getItem().stream()
                             .map(ImageListItem::getOriginImgUrl)
                             .filter(url -> url != null && !url.isBlank())
                             .forEach(images::add);
        } catch (Exception e) {
            log.warn("이미지 조회 실패: placeId={}, error={}", placeId, e.getMessage());
        }

        // 캐시 저장
        PlaceDetailCache detailCache = PlaceDetailCache.builder()
                                                       .placeId(placeId)
                                                       .contentTypeId(contentTypeId)
                                                       .phoneNumber(infoCenter)
                                                       .images(images)
                                                       .restDate(restDate)
                                                       .useTime(useTime)
                                                       .scale(scale)
                                                       .openDate(openDate)
                                                       .useSeason(useSeason)
                                                       .expAgeRange(expAgeRange)
                                                       .expGuide(expGuide)
                                                       .heritage1(heritage1)
                                                       .accomCount(accomCount)
                                                       .chkBabyCarriage(chkBabyCarriage)
                                                       .chkPet(chkPet)
                                                       .firstMenu(firstMenu)
                                                       .treatMenu(treatMenu)
                                                       .seat(seat)
                                                       .smoking(smoking)
                                                       .packing(packing)
                                                       .kidsFacility(kidsFacility)
                                                       .openDateFood(openDateFood)
                                                       .spendTime(spendTime)
                                                       .useFee(useFee)
                                                       .discountInfo(discountInfo)
                                                       .accomCountCulture(accomCountCulture)
                                                       .parkingCulture(parkingCulture)
                                                       .openPeriod(openPeriod)
                                                       .useFeeLeports(useFeeLeports)
                                                       .reservation(reservation)
                                                       .scaleLeports(scaleLeports)
                                                       .expAgeRangeLeports(expAgeRangeLeports)
                                                       .openTime(openTime)
                                                       .saleItem(saleItem)
                                                       .shopGuide(shopGuide)
                                                       .scaleShopping(scaleShopping)
                                                       .fairDay(fairDay)
                                                       .build();

        String detailKey = "place:detail:" + placeId;
        redisRepository.setValue(detailKey, detailCache, DETAIL_CACHE_TTL);

        return detailCache;
    }

    /**
     * 카테고리명으로 콘텐츠 타입 ID 추출
     */
    private String extractContentTypeId(final String category) {
        if (category == null) return "12";

        return switch (category) {
            case "관광지" -> "12";
            case "문화시설" -> "14";
            case "레포츠" -> "28";
            case "쇼핑" -> "38";
            case "음식점" -> "39";
            default -> "12";
        };
    }

    /**
     * 콘텐츠 타입별 이용시간 필드 추출
     */
    private String getUseTimeByContentType(final DetailInfoItem detail, final String contentTypeId) {
        return switch (contentTypeId) {
            case "12" -> detail.getUseTime();
            case "14" -> detail.getUseTimeCulture();
            case "28" -> detail.getUseTimeLeports();
            case "38" -> detail.getOpenTime();
            case "39" -> detail.getOpenTimeFood();
            default -> null;
        };
    }

    /**
     * VisitKorea contentTypeId → 서비스 카테고리 매핑
     */
    private String mapContentTypeIdToCategory(final String contentTypeId) {
        if (contentTypeId == null) return "기타";

        try {
            ContentType contentType = ContentType.from(contentTypeId);
            return switch (contentType.getContentTypeId()) {
                case "12" -> "관광지";
                case "14" -> "문화시설";
                case "28" -> "레포츠";
                case "38" -> "쇼핑";
                case "39" -> "음식점";
                default -> "기타";
            };
        } catch (Exception e) {
            return "기타";
        }
    }

    /**
     * 두 좌표 간 거리 계산 (Haversine 공식, 단위: 미터)
     */
    private double calculateDistance(final double lat1, final double lon1, final double lat2, final double lon2) {
        final int R = 6371000; // 지구 반지름 (미터)

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * 도보 소요 시간 계산 (분)
     */
    private int calculateWalkTime(final double distance) {
        return (int) Math.ceil(distance / WALK_SPEED_PER_MINUTE);
    }

}
