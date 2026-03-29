package com.timespot.backend.domain.place.service;

import static com.timespot.backend.common.response.ErrorCode.PLACE_NOT_FOUND;
import static com.timespot.backend.common.response.ErrorCode.STATION_NOT_FOUND;
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
import com.timespot.backend.domain.station.dao.StationRepository;
import com.timespot.backend.domain.station.model.Station;
import com.timespot.backend.infra.redis.dao.RedisGeoRepository;
import com.timespot.backend.infra.redis.dao.RedisRepository;
import com.timespot.backend.infra.redis.model.GeoPlace;
import com.timespot.backend.infra.redis.model.PlaceCardCache;
import com.timespot.backend.infra.redis.model.PlaceDetailCache;
import com.timespot.backend.infra.visitkorea.client.VisitKoreaApiClient;
import com.timespot.backend.infra.visitkorea.client.properties.VisitKoreaProperties;
import com.timespot.backend.infra.visitkorea.dto.VisitKoreaResponseDto;
import com.timespot.backend.infra.visitkorea.dto.VisitKoreaResponseDto.DetailInfoItem;
import com.timespot.backend.infra.visitkorea.dto.VisitKoreaResponseDto.ImageListResponse;
import com.timespot.backend.infra.visitkorea.dto.VisitKoreaResponseDto.Item;
import com.timespot.backend.infra.visitkorea.model.Category1Type;
import com.timespot.backend.infra.visitkorea.model.ContentType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
        Station station = validateStation(stationId);

        int searchRadius = visitKoreaProperties.getMaxRadiusMeters();

        List<GeoPlace> geoPlaces = getPlacesFromGeoOrApi(station, searchRadius);

        List<GeoPlace> filtered = filterPlaces(geoPlaces, keyword, category);

        List<GeoPlace> sorted = sortPlaces(
                filtered, pageable.getSort(), userLat, userLon, mapLat, mapLon
        );

        return buildAvailablePlacePage(sorted, pageable, userLat, userLon, remainingMinutes);
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
     * GEO 에서 장소 조회 (캐시 없으면 API 호출)
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

        if (geoPlaces.isEmpty()) return syncFromVisitKorea(station, searchRadius);

        return geoPlaces;
    }

    /**
     * VisitKorea API 에서 장소 동기화
     */
    private List<GeoPlace> syncFromVisitKorea(final Station station, final int searchRadius) {
        log.info("VisitKorea API 동기화 시작: stationId={}, radius={}", station.getId(), searchRadius);

        List<GeoPlace> allPlaces  = new ArrayList<>();
        Set<String>    placeIdSet = new HashSet<>();
        String         geoKey     = "place:geo:" + station.getId();

        for (ContentType contentType : ContentType.getAllContentTypes()) {
            for (int page = 1; page <= visitKoreaProperties.getSyncPages(); page++) {
                VisitKoreaResponseDto.InfoListResponse response = visitKoreaApiClient.locationBasedList(
                        station.getLongitude(),
                        station.getLatitude(),
                        searchRadius,
                        contentType,
                        page,
                        visitKoreaProperties.getPageSize()
                );

                if (!response.isSuccess() || response.body().items().item() == null) break;

                for (Item item : response.body().items().item()) {
                    if (item.mapX() == null || item.mapY() == null) continue;
                    if (item.dist() != null && item.dist() > searchRadius) continue;

                    String placeId = item.contentId();
                    if (!placeIdSet.add(placeId)) continue;

                    redisGeoRepository.addPlace(geoKey, placeId, item.mapX(), item.mapY());

                    PlaceCardCache cache = new PlaceCardCache(
                            placeId,
                            item.getName(),
                            mapCat1ToCategory(item.cat1()),
                            item.getFullAddress(),
                            item.mapY(),
                            item.mapX(),
                            item.dist() != null ? item.dist() : 0.0,
                            item.firstImage()
                    );
                    savePlaceCardCache(placeId, cache);

                    allPlaces.add(GeoPlace.builder()
                                          .placeId(placeId)
                                          .latitude(item.mapY())
                                          .longitude(item.mapX())
                                          .distance(item.dist() != null ? item.dist() : 0.0)
                                          .build());
                }

                if (response.body().items().item().size() < visitKoreaProperties.getPageSize()) break;
            }
        }
        log.info("VisitKorea 동기화 완료: stationId={}, count={}", station.getId(), allPlaces.size());

        return allPlaces;
    }

    /**
     * 장소 필터링 (키워드, 카테고리)
     */
    private List<GeoPlace> filterPlaces(final List<GeoPlace> places,
                                        final String keyword,
                                        final String category) {
        return places.stream()
                     .filter(place -> {
                         if (keyword != null && !keyword.isBlank()) {
                             PlaceCardCache cache = getPlaceCardCache(place.getPlaceId()).orElse(null);
                             if (cache == null) return false;
                             return cache.getName().contains(keyword) || cache.getCategory().contains(keyword);
                         }
                         return true;
                     })
                     .filter(place -> {
                         if (category == null || "전체".equals(category)) return true;
                         PlaceCardCache cache = getPlaceCardCache(place.getPlaceId()).orElse(null);
                         return cache != null && category.equals(cache.getCategory());
                     })
                     .toList();
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

            if ("distanceFromUser".equals(property)) {
                return places.stream()
                             .sorted(Comparator.comparingDouble(
                                     place -> calculateDistance(userLat,
                                                                userLon,
                                                                place.getLatitude(),
                                                                place.getLongitude())
                             ))
                             .toList();
            }

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
        int end   = Math.min(start + pageable.getPageSize(), places.size());

        List<AvailablePlace> content = places.stream()
                                             .skip(start)
                                             .limit(pageable.getPageSize())
                                             .map(place -> buildAvailablePlace(
                                                     place, userLat, userLon, remainingMinutes
                                             ))
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
        PlaceCardCache cardInfo = getPlaceCardCache(geoPlace.getPlaceId())
                .orElseThrow(() -> new GlobalException(PLACE_NOT_FOUND));

        double distanceFromUser = calculateDistance(
                userLat, userLon, geoPlace.getLatitude(), geoPlace.getLongitude()
        );

        int walkTimeFromStation = calculateWalkTime(geoPlace.getDistance());
        int stayableMinutes     = calculateStayableMinutes(remainingMinutes, walkTimeFromStation);

        boolean visitable = stayableMinutes >= 0;

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

        boolean visitable = stayableMinutes >= 0;

        return switch (category) {
            case "관광지" -> buildTouristPlaceDetail(
                    cardInfo, detailInfo, station, distanceFromStation, walkTimeFromStation, stayableMinutes, visitable, leaveTime
            );
            case "음식점" -> buildRestaurantDetail(
                    cardInfo, detailInfo, station, distanceFromStation, walkTimeFromStation, stayableMinutes, visitable, leaveTime
            );
            case "문화시설" -> buildCulturePlaceDetail(
                    cardInfo, detailInfo, station, distanceFromStation, walkTimeFromStation, stayableMinutes, visitable, leaveTime
            );
            case "레포츠" -> buildSportsPlaceDetail(
                    cardInfo, detailInfo, station, distanceFromStation, walkTimeFromStation, stayableMinutes, visitable, leaveTime
            );
            case "쇼핑" -> buildShoppingPlaceDetail(
                    cardInfo, detailInfo, station, distanceFromStation, walkTimeFromStation, stayableMinutes, visitable, leaveTime
            );
            default -> buildTouristPlaceDetail(
                    cardInfo, detailInfo, station, distanceFromStation, walkTimeFromStation, stayableMinutes, visitable, leaveTime
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
        return redisRepository.getValue(key, PlaceCardCache.class);
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
        VisitKoreaResponseDto.DetailInfoResponse detailResponse = visitKoreaApiClient.detailIntro(
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

        if (detailResponse.isSuccess() && detailResponse.body().items().item() != null &&
            !detailResponse.body().items().item().isEmpty()) {
            DetailInfoItem detail = detailResponse.body().items().item().get(0);

            // 공통 필드
            infoCenter = detail.infoCenter();
            restDate = detail.restDate();
            scale = detail.scale();

            // 타입별 필드 추출
            openDate = detail.openDate();
            useSeason = detail.useSeason();
            expAgeRange = detail.expAgeRange();
            expGuide = detail.expGuide();
            heritage1 = detail.heritage1();
            accomCount = detail.accomCount();
            chkBabyCarriage = detail.chkBabyCarriage();
            chkPet = detail.chkPet();

            firstMenu = detail.firstMenu();
            treatMenu = detail.treatMenu();
            seat = detail.seat();
            smoking = detail.smoking();
            packing = detail.packing();
            kidsFacility = detail.kidsFacility();
            openDateFood = detail.openDateFood();

            spendTime = detail.spendTime();
            useFee = detail.useFee();
            discountInfo = detail.discountInfo();
            accomCountCulture = detail.accomCountCulture();
            parkingCulture = detail.parkingCulture();

            openPeriod = detail.openPeriod();
            useFeeLeports = detail.useFeeLeports();
            reservation = detail.reservation();
            scaleLeports = detail.scaleLeports();
            expAgeRangeLeports = detail.expAgeRangeLeports();

            openTime = detail.openTime();
            saleItem = detail.saleItem();
            shopGuide = detail.shopGuide();
            scaleShopping = detail.scaleShopping();
            fairDay = detail.fairDay();

            // 이용시간은 타입별로 필드가 다름
            useTime = getUseTimeByContentType(detail, contentTypeId);
        }

        List<String> images = new ArrayList<>();
        if (cardInfo.getImageUrl() != null && !cardInfo.getImageUrl().isEmpty()) {
            images.add(cardInfo.getImageUrl());
        }

        try {
            ImageListResponse imageResponse = visitKoreaApiClient.detailImage(placeId, 1, 10);

            if (imageResponse.isSuccess() && imageResponse.body().items().item() != null) {
                imageResponse.body().items().item().stream()
                             .map(VisitKoreaResponseDto.ImageItem::originImgUrl)
                             .filter(url -> url != null && !url.isEmpty())
                             .forEach(images::add);
            }
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
            case "12" -> detail.useTime();
            case "14" -> detail.useTimeCulture();
            case "28" -> detail.useTimeLeports();
            case "38" -> detail.openTime();
            case "39" -> detail.openTimeFood();
            default -> null;
        };
    }

    /**
     * VisitKorea cat1 → 서비스 카테고리 매핑
     */
    private String mapCat1ToCategory(final String cat1) {
        if (cat1 == null) return "기타";

        try {
            Category1Type category1Type = Category1Type.from(cat1);
            return category1Type.getDescription();
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
