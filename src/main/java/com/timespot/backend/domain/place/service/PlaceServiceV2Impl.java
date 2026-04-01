package com.timespot.backend.domain.place.service;

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
import com.timespot.backend.infra.redis.model.GeoPlace;
import com.timespot.backend.infra.redis.model.PlaceCardCache;
import com.timespot.backend.infra.redis.model.PlaceDetailCache;
import com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst;
import com.timespot.backend.infra.visitkorea.service.VisitKoreaPlaceService;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
 * 26. 4. 1.     loadingKKamo21       VisitKoreaPlaceService 의존성 추가 및 중복 로직 제거
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PlaceServiceV2Impl implements PlaceServiceV2 {

    private final StationRepository      stationRepository;
    private final VisitKoreaPlaceService visitKoreaPlaceService;

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

        int searchRadius = VisitKoreaConst.MAX_RADIUS_METERS;

        List<GeoPlace> geoPlaces = visitKoreaPlaceService.findPlacesWithinRadius(
                VisitKoreaConst.GEO_KEY_PREFIX + stationId,
                station.getLongitude(),
                station.getLatitude(),
                searchRadius
        );
        log.info("GEO 검색 결과: stationId={}, count={}", station.getId(), geoPlaces.size());

        if (geoPlaces.isEmpty()) {
            log.info("GEO 에 데이터가 없어 VisitKorea API 동기화 수행: stationId={}", station.getId());
            List<GeoPlace> syncedPlaces = visitKoreaPlaceService.syncPlacesFromVisitKorea(
                    station.getId(),
                    station.getLongitude(),
                    station.getLatitude(),
                    searchRadius
            );
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

        PlaceCardCache   cardInfo   = visitKoreaPlaceService.getPlaceCardWithFallback(placeId);
        PlaceDetailCache detailInfo = visitKoreaPlaceService.getPlaceDetailWithFallback(placeId);

        Station station = validateStation(stationId);

        double distanceFromStation = calculateDistance(
                station.getLatitude(),
                station.getLongitude(),
                cardInfo.getLatitude(),
                cardInfo.getLongitude()
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
        if (category == null || category.isBlank()) return filterByKeywordOnly(places, keyword);

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
                     .filter(place -> {
                         PlaceCardCache cache = visitKoreaPlaceService.getPlaceCardCache(place.getPlaceId())
                                                                      .orElse(null);
                         return cache != null
                                && matchesKeyword(cache, keyword)
                                && matchesCategory(cache, placeCategory)
                                && matchesCafeKeyword(cache, placeCategory);
                     })
                     .toList();
    }

    /**
     * 카테고리 미지정 시 키워드만 필터링
     */
    private List<GeoPlace> filterByKeywordOnly(final List<GeoPlace> places, final String keyword) {
        if (keyword == null || keyword.isBlank()) return places;
        log.debug("키워드 필터링만 수행: keyword={}", keyword);
        return places.stream()
                     .filter(place -> {
                         PlaceCardCache cache = visitKoreaPlaceService.getPlaceCardCache(place.getPlaceId())
                                                                      .orElse(null);
                         return cache != null && matchesKeyword(cache, keyword);
                     })
                     .toList();
    }

    /**
     * 키워드 매칭 확인 (장소명 또는 카테고리)
     */
    private boolean matchesKeyword(final PlaceCardCache cache, final String keyword) {
        if (keyword == null || keyword.isBlank()) return true;
        return cache.getName().contains(keyword) || cache.getCategory().contains(keyword);
    }

    /**
     * 서버 카테고리 매칭 확인
     */
    private boolean matchesCategory(final PlaceCardCache cache, final PlaceCategory placeCategory) {
        if (placeCategory == PlaceCategory.CAFE) return "음식점".equals(cache.getCategory());
        return placeCategory.getServerCategories().contains(cache.getCategory());
    }

    /**
     * 카페 키워드 필터링
     * RESTAURANT: '카페' 키워드 제외
     * CAFE: '카페' 키워드 포함만
     */
    private boolean matchesCafeKeyword(final PlaceCardCache cache, final PlaceCategory placeCategory) {
        if (!placeCategory.requiresCafeKeywordFilter()) return true;

        String name     = cache.getName();
        String category = cache.getCategory();

        boolean isCafeCategory   = "카페".equals(category);
        boolean nameContainsCafe = name != null && name.contains("카페");

        if (placeCategory.isCafeOnly()) return isCafeCategory || nameContainsCafe;
        else return !isCafeCategory && !nameContainsCafe;
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

        PlaceCardCache cardInfo = visitKoreaPlaceService.getPlaceCardWithFallback(geoPlace.getPlaceId());

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
     * <p>
     * visitable 과 isOpened 를 분리하여 반환합니다:
     * visitable: 시간/거리 기반 (캐싱됨)
     * isOpened: 실시간 영업 상태 (캐싱 제외, 매번 계산)
     * </p>
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

        Boolean isOpened            = calculateIsOpened(detailInfo);
        String  googleOpeningStatus = isOpened != null ? (isOpened ? "영업 중" : "영업 종료") : "알 수 없음";

        log.debug("PlaceDetail 빌드: placeName={}, visitable={}, isOpened={}, googleOpeningStatus={}",
                  cardInfo.getName(), visitable, isOpened, googleOpeningStatus);

        return switch (category) {
            case "관광지" -> buildTouristPlaceDetail(
                    cardInfo, detailInfo, station, distanceFromStation, walkTimeFromStation, stayableMinutes,
                    visitable, leaveTime, isOpened, googleOpeningStatus
            );
            case "음식점" -> buildRestaurantDetail(
                    cardInfo, detailInfo, station, distanceFromStation, walkTimeFromStation, stayableMinutes,
                    visitable, leaveTime, isOpened, googleOpeningStatus
            );
            case "문화시설" -> buildCulturePlaceDetail(
                    cardInfo, detailInfo, station, distanceFromStation, walkTimeFromStation, stayableMinutes,
                    visitable, leaveTime, isOpened, googleOpeningStatus
            );
            case "레포츠" -> buildSportsPlaceDetail(
                    cardInfo, detailInfo, station, distanceFromStation, walkTimeFromStation, stayableMinutes,
                    visitable, leaveTime, isOpened, googleOpeningStatus
            );
            case "쇼핑" -> buildShoppingPlaceDetail(
                    cardInfo, detailInfo, station, distanceFromStation, walkTimeFromStation, stayableMinutes,
                    visitable, leaveTime, isOpened, googleOpeningStatus
            );
            default -> buildTouristPlaceDetail(
                    cardInfo, detailInfo, station, distanceFromStation, walkTimeFromStation, stayableMinutes,
                    visitable, leaveTime, isOpened, googleOpeningStatus
            );
        };
    }

    /**
     * 실시간 영업 상태 (isOpened) 계산
     * <p>
     * 캐싱된 요일별 영업 시간으로 현재 시간과 비교
     * </p>
     *
     * @param detailInfo 장소 상세 정보 캐시
     * @return 현재 영업 중 여부 (null 일 수 있음)
     */
    private Boolean calculateIsOpened(final PlaceDetailCache detailInfo) {
        if (detailInfo.getGoogleWeekdayDescriptions() != null &&
            detailInfo.getGoogleWeekdayDescriptions().length > 0) {
            Boolean isOpen = calculateIsOpenedFromWeekday(detailInfo.getGoogleWeekdayDescriptions());
            if (isOpen != null) {
                log.debug("isOpened 계산: Google 영업 시간 사용={}", isOpen);
                return isOpen;
            }
        }

        log.debug("isOpened 계산: 정보 없음 (null)");
        return null;
    }

    /**
     * 요일별 영업 시간으로 현재 영업 중 여부 계산
     *
     * @param weekdayDescriptions 요일별 영업 시간 (예: "월요일: 오전 9:00 ~ 오후 6:00")
     * @return 영업 중 여부 (파싱 실패 시 null)
     */
    private Boolean calculateIsOpenedFromWeekday(final String[] weekdayDescriptions) {
        try {
            LocalDateTime now       = LocalDateTime.now();
            String        todayDesc = getTodayWeekdayDescription(weekdayDescriptions, now);

            log.info("오늘의 영업 시간: todayDesc={}", todayDesc);

            if (todayDesc == null || todayDesc.isBlank()) {
                log.warn("오늘의 영업 시간을 찾을 수 없음");
                return null;
            }

            Boolean result = parseOpeningHours(todayDesc, now);
            log.info("영업 상태 계산 결과: now={}, todayDesc={}, isOpened={}", now, todayDesc, result);
            return result;
        } catch (Exception e) {
            log.warn("요일별 영업 시간 파싱 실패: error={}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 오늘 요일에 해당하는 영업 시간 설명 추출
     */
    private String getTodayWeekdayDescription(final String[] weekdayDescriptions,
                                              final LocalDateTime now) {
        int      dayOfWeek   = now.getDayOfWeek().getValue();
        String[] koreanDays  = {"월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일"};
        String   todayKorean = koreanDays[dayOfWeek - 1];

        log.info("요일 매칭 시도: now={}, dayOfWeek={}, todayKorean={}", now, dayOfWeek, todayKorean);

        for (String desc : weekdayDescriptions) {
            log.info("요일 확인: desc={}", desc);
            if (desc.contains(todayKorean)) {
                String result = desc.contains(":") ? desc.substring(desc.indexOf(":") + 1).trim() : desc;
                log.info("요일 매칭 성공: todayKorean={}, result={}", todayKorean, result);
                return result;
            }
        }

        log.warn("요일 매칭 실패: todayKorean={}, weekdayDescriptions={}", todayKorean, weekdayDescriptions);
        return null;
    }

    /**
     * 영업 시간 문자열 파싱 (예: "오전 9:00 ~ 오후 6:00", "AM 5:23 ~ PM 5:23")
     *
     * @param timeDesc 영업 시간 설명
     * @param now      현재 시간
     * @return 영업 중 여부
     */
    private Boolean parseOpeningHours(final String timeDesc, final LocalDateTime now) {
        try {
            log.info("영업 시간 파싱 시작: timeDesc={}, now={}", timeDesc, now);

            String[] parts = timeDesc.split("~");
            if (parts.length != 2) {
                log.warn("영업 시간 형식 오류: timeDesc={}, parts.length={}", timeDesc, parts.length);
                return null;
            }

            LocalTime openTime  = parseKoreanTime(parts[0].trim());
            LocalTime closeTime = parseKoreanTime(parts[1].trim());

            log.info("영업 시간 파싱 완료: openTime={}, closeTime={}", openTime, closeTime);

            if (openTime == null || closeTime == null) {
                log.warn("영업 시간 파싱 실패: openTime={}, closeTime={}", openTime, closeTime);
                return null;
            }

            LocalTime currentTime = now.toLocalTime();
            log.info("현재 시간 비교: currentTime={}, openTime={}, closeTime={}", currentTime, openTime, closeTime);

            boolean isOpen;
            if (closeTime.isBefore(openTime)) isOpen = currentTime.isAfter(openTime) || currentTime.isBefore(closeTime);
            else isOpen = !currentTime.isBefore(openTime) && currentTime.isBefore(closeTime);

            log.info("영업 상태 판단: isOpen={}", isOpen);
            return isOpen;
        } catch (Exception e) {
            log.warn("영업 시간 파싱 중 예외: error={}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 한국어/영어 시간 문자열 파싱 (예: "오전 9:00", "오후 6:00", "AM 5:23", "PM 5:23")
     */
    private LocalTime parseKoreanTime(final String timeStr) {
        try {
            String time = timeStr.trim().toUpperCase();

            if (time.startsWith("AM") || time.startsWith("PM")) {
                boolean isPM = time.startsWith("PM");
                time = time.replace("AM", "").replace("PM", "").trim();

                String[] parts  = time.split(":");
                int      hour   = Integer.parseInt(parts[0].trim());
                int      minute = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;

                if (isPM && hour != 12) hour += 12;
                else if (!isPM && hour == 12) hour = 0;

                log.debug("시간 파싱 (AM/PM): {} -> {}:{}", timeStr, hour, minute);
                return LocalTime.of(hour, minute);
            }

            if (time.contains("오전") || time.contains("오후")) {
                boolean isPM = time.contains("오후");
                time = time.replace("오전", "").replace("오후", "").trim();

                String[] parts  = time.split(":");
                int      hour   = Integer.parseInt(parts[0].trim());
                int      minute = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;

                if (isPM && hour != 12) hour += 12;
                else if (!isPM && hour == 12) hour = 0;

                log.debug("시간 파싱 (오전/오후): {} -> {}:{}", timeStr, hour, minute);
                return LocalTime.of(hour, minute);
            }

            String[] parts  = time.split(":");
            int      hour   = Integer.parseInt(parts[0].trim());
            int      minute = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;

            log.debug("시간 파싱 (숫자만): {} -> {}:{}", timeStr, hour, minute);
            return LocalTime.of(hour, minute);
        } catch (Exception e) {
            log.warn("시간 파싱 실패: timeStr={}, error={}", timeStr, e.getMessage());
            return null;
        }
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
            final LocalDateTime leaveTime,
            final Boolean isOpened,
            final String googleOpeningStatus
    ) {
        return new TouristPlaceDetail(
                "TOURIST",
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
                detailInfo.getGoogleWeekdayDescriptions(),
                isOpened,
                googleOpeningStatus,
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
            final LocalDateTime leaveTime,
            final Boolean isOpened,
            final String googleOpeningStatus
    ) {
        return new RestaurantDetail(
                "RESTAURANT",
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
                detailInfo.getGoogleWeekdayDescriptions(),
                isOpened,
                googleOpeningStatus,
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
            final LocalDateTime leaveTime,
            final Boolean isOpened,
            final String googleOpeningStatus
    ) {
        return new CulturePlaceDetail(
                "CULTURE",
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
                detailInfo.getGoogleWeekdayDescriptions(),
                isOpened,
                googleOpeningStatus,
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
            final LocalDateTime leaveTime,
            final Boolean isOpened,
            final String googleOpeningStatus
    ) {
        return new SportsPlaceDetail(
                "SPORTS",
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
                detailInfo.getGoogleWeekdayDescriptions(),
                isOpened,
                googleOpeningStatus,
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
            final LocalDateTime leaveTime,
            final Boolean isOpened,
            final String googleOpeningStatus
    ) {
        return new ShoppingPlaceDetail(
                "SHOPPING",
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
                detailInfo.getGoogleWeekdayDescriptions(),
                isOpened,
                googleOpeningStatus,
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
