package com.timespot.backend.domain.place.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;
import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.place.dto
 * FileName    : PlaceResponseDtoV2
 * Author      : loadingKKamo21
 * Date        : 26. 3. 29.
 * Description : Place API V2 응답 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 29.    loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@JsonInclude(NON_NULL)
@Schema(description = "장소 API V2 응답 페이로드")
public abstract class PlaceResponseDtoV2 {

    /**
     * 방문 가능한 장소 응답 (목록용)
     */
    @Getter
    @RequiredArgsConstructor
    @Schema(description = "방문 가능 장소 응답")
    public static class AvailablePlace {

        @Schema(description = "장소 ID", example = "126644")
        private final String placeId;

        @Schema(description = "장소 이름", example = "남산서울타워")
        private final String name;

        @Schema(description = "장소 카테고리", example = "관광지")
        private final String category;

        @Schema(description = "도로명 주소", example = "서울특별시 용산구 동자동 11-111")
        private final String address;

        @Schema(description = "위도", example = "37.5546")
        private final Double latitude;

        @Schema(description = "경도", example = "126.9706")
        private final Double longitude;

        @Schema(description = "사용자로부터의 거리 (미터)", example = "150.5")
        private final Double distanceFromUser;

        @Schema(description = "역으로부터의 거리 (미터)", example = "200.0")
        private final Double distanceFromStation;

        @Schema(description = "역에서 도보 소요 시간 (분)", example = "4")
        private final Integer walkTimeFromStation;

        @Schema(description = "체류 가능 시간 (분)", example = "25")
        private final Integer stayableMinutes;

        @Schema(description = "방문 가능 여부", example = "true")
        private final Boolean visitable;

        @Schema(description = "대표 이미지 URL", example = "https://...")
        private final String imageUrl;

    }

    // ======================= 장소 상세 정보 (타입별) =======================

    /**
     * 장소 상세 정보 공통 인터페이스
     */
    @Getter
    @JsonTypeInfo(
            use = NAME,
            include = PROPERTY,
            property = "placeType",
            visible = true
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = TouristPlaceDetail.class, name = "TOURIST"),
            @JsonSubTypes.Type(value = RestaurantDetail.class, name = "RESTAURANT"),
            @JsonSubTypes.Type(value = CulturePlaceDetail.class, name = "CULTURE"),
            @JsonSubTypes.Type(value = SportsPlaceDetail.class, name = "SPORTS"),
            @JsonSubTypes.Type(value = ShoppingPlaceDetail.class, name = "SHOPPING")
    })
    @Schema(description = "장소 상세 정보")
    public abstract static class PlaceDetail {

        @Schema(description = "장소 타입",
                example = "TOURIST",
                allowableValues = {"TOURIST", "RESTAURANT", "CULTURE", "SPORTS", "SHOPPING"})
        @JsonProperty("placeType")
        protected final String placeType;

        @Schema(description = "장소 ID", example = "126644")
        private final String placeId;

        @Schema(description = "장소 이름", example = "남산서울타워")
        private final String name;

        @Schema(description = "장소 카테고리", example = "관광지")
        private final String category;

        @Schema(description = "도로명 주소", example = "서울특별시 용산구 동자동 11-111")
        private final String address;

        @Schema(description = "위도", example = "37.5546")
        private final Double latitude;

        @Schema(description = "경도", example = "126.9706")
        private final Double longitude;

        @Schema(description = "역으로부터의 직선 거리 (미터)", example = "200.0")
        private final Integer distanceFromStation;

        @Schema(description = "역에서 도보 소요 시간 (분)", example = "4")
        private final Integer walkTimeFromStation;

        @Schema(description = "체류 가능 시간 (분)", example = "25")
        private final Integer stayableMinutes;

        @Schema(description = "방문 가능 여부", example = "true")
        private final Boolean visitable;

        @Schema(description = "기준 역 위도", example = "37.5546")
        private final Double stationLatitude;

        @Schema(description = "기준 역 경도", example = "126.9706")
        private final Double stationLongitude;

        @Schema(description = "역으로 출발해야 하는 시간 (yyyy-MM-dd HH:mm:ss)", example = "2026-03-29 20:30:00")
        private final LocalDateTime leaveTime;

        @Schema(description = "대표 이미지 URL 목록", example = "[\"https://...\", \"https://...\"]")
        private final List<String> images;

        @Schema(description = "전화번호", example = "02-1234-5678")
        private final String phoneNumber;

        @Schema(description = "휴무일", example = "연중무휴")
        private final String restDate;

        @Schema(description = "이용시간/영업시간", example = "09:00 - 18:00")
        private final String useTime;

        @Schema(description = "Google 영업 상태", example = "영업 중")
        private final String googleOpeningStatus;

        @Schema(description = "Google 다음 닫는 시간 (yyyy-MM-dd HH:mm:ss)", example = "2026-03-30 22:00:00")
        private final LocalDateTime googleNextClosingTime;

        @Schema(description = "Google 요일별 운영 시간", example = "[\"월요일: 오전 9:00 ~ 오후 6:00\", \"화요일: 오전 9:00 ~ 오후 6:00\"]")
        private final String[] googleWeekdayDescriptions;

        protected PlaceDetail(final String placeType,
                              final String placeId,
                              final String name,
                              final String category,
                              final String address,
                              final Double latitude,
                              final Double longitude,
                              final Integer distanceFromStation,
                              final Integer walkTimeFromStation,
                              final Integer stayableMinutes,
                              final Boolean visitable,
                              final Double stationLatitude,
                              final Double stationLongitude,
                              final LocalDateTime leaveTime,
                              final List<String> images,
                              final String phoneNumber,
                              final String restDate,
                              final String useTime,
                              final String googleOpeningStatus,
                              final LocalDateTime googleNextClosingTime,
                              final String[] googleWeekdayDescriptions) {
            this.placeType = placeType;
            this.placeId = placeId;
            this.name = name;
            this.category = category;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.distanceFromStation = distanceFromStation;
            this.walkTimeFromStation = walkTimeFromStation;
            this.stayableMinutes = stayableMinutes;
            this.visitable = visitable;
            this.stationLatitude = stationLatitude;
            this.stationLongitude = stationLongitude;
            this.leaveTime = leaveTime;
            this.images = images;
            this.phoneNumber = phoneNumber;
            this.restDate = restDate;
            this.useTime = useTime;
            this.googleOpeningStatus = googleOpeningStatus;
            this.googleNextClosingTime = googleNextClosingTime;
            this.googleWeekdayDescriptions = googleWeekdayDescriptions;
        }

    }

    /**
     * 관광지 상세 정보
     */
    @Getter
    @Schema(description = "관광지 상세 정보")
    public static class TouristPlaceDetail extends PlaceDetail {

        @Schema(description = "개장일", example = "2020-01-01")
        private final String openDate;

        @Schema(description = "이용시기", example = "연중무휴")
        private final String useSeason;

        @Schema(description = "체험가능연령", example = "전연령")
        private final String expAgeRange;

        @Schema(description = "체험안내", example = "가이드 투어 가능")
        private final String expGuide;

        @Schema(description = "세계문화유산유무", example = "Y")
        private final String heritage1;

        @Schema(description = "수용인원", example = "500")
        private final String accomCount;

        @Schema(description = "유모차대여정보", example = "가능")
        private final String chkBabyCarriage;

        @Schema(description = "애완동물동반가능정보", example = "불가")
        private final String chkPet;

        public TouristPlaceDetail(final String placeId,
                                  final String name,
                                  final String category,
                                  final String address,
                                  final Double latitude,
                                  final Double longitude,
                                  final Integer distanceFromStation,
                                  final Integer walkTimeFromStation,
                                  final Integer stayableMinutes,
                                  final Boolean visitable,
                                  final Double stationLatitude,
                                  final Double stationLongitude,
                                  final LocalDateTime leaveTime,
                                  final List<String> images,
                                  final String phoneNumber,
                                  final String restDate,
                                  final String useTime,
                                  final String openDate,
                                  final String useSeason,
                                  final String expAgeRange,
                                  final String expGuide,
                                  final String heritage1,
                                  final String accomCount,
                                  final String chkBabyCarriage,
                                  final String chkPet) {
            this("TOURIST", placeId, name, category, address, latitude, longitude, distanceFromStation,
                 walkTimeFromStation, stayableMinutes, visitable, stationLatitude, stationLongitude, leaveTime, images,
                 phoneNumber, restDate, useTime, null, null, null, openDate, useSeason, expAgeRange, expGuide,
                 heritage1, accomCount, chkBabyCarriage, chkPet);
        }

        public TouristPlaceDetail(final String placeType,
                                  final String placeId,
                                  final String name,
                                  final String category,
                                  final String address,
                                  final Double latitude,
                                  final Double longitude,
                                  final Integer distanceFromStation,
                                  final Integer walkTimeFromStation,
                                  final Integer stayableMinutes,
                                  final Boolean visitable,
                                  final Double stationLatitude,
                                  final Double stationLongitude,
                                  final LocalDateTime leaveTime,
                                  final List<String> images,
                                  final String phoneNumber,
                                  final String restDate,
                                  final String useTime,
                                  final String googleOpeningStatus,
                                  final LocalDateTime googleNextClosingTime,
                                  final String[] googleWeekdayDescriptions,
                                  final String openDate,
                                  final String useSeason,
                                  final String expAgeRange,
                                  final String expGuide,
                                  final String heritage1,
                                  final String accomCount,
                                  final String chkBabyCarriage,
                                  final String chkPet) {
            super(placeType, placeId, name, category, address, latitude, longitude, distanceFromStation,
                  walkTimeFromStation, stayableMinutes, visitable, stationLatitude, stationLongitude, leaveTime, images,
                  phoneNumber, restDate, useTime, googleOpeningStatus, googleNextClosingTime,
                  googleWeekdayDescriptions);
            this.openDate = openDate;
            this.useSeason = useSeason;
            this.expAgeRange = expAgeRange;
            this.expGuide = expGuide;
            this.heritage1 = heritage1;
            this.accomCount = accomCount;
            this.chkBabyCarriage = chkBabyCarriage;
            this.chkPet = chkPet;
        }

    }

    /**
     * 음식점 상세 정보
     */
    @Getter
    @Schema(description = "음식점 상세 정보")
    public static class RestaurantDetail extends PlaceDetail {

        @Schema(description = "대표메뉴", example = "김치찌개, 된장찌개")
        private final String firstMenu;

        @Schema(description = "취급메뉴", example = "한식")
        private final String treatMenu;

        @Schema(description = "좌석수", example = "50")
        private final String seat;

        @Schema(description = "금연/흡연여부", example = "금연")
        private final String smoking;

        @Schema(description = "포장가능", example = "Y")
        private final String packing;

        @Schema(description = "어린이놀이방여부", example = "Y")
        private final String kidsFacility;

        @Schema(description = "개업일", example = "2015-03-01")
        private final String openDateFood;

        public RestaurantDetail(final String placeId,
                                final String name,
                                final String category,
                                final String address,
                                final Double latitude,
                                final Double longitude,
                                final Integer distanceFromStation,
                                final Integer walkTimeFromStation,
                                final Integer stayableMinutes,
                                final Boolean visitable,
                                final Double stationLatitude,
                                final Double stationLongitude,
                                final LocalDateTime leaveTime,
                                final List<String> images,
                                final String phoneNumber,
                                final String restDate,
                                final String useTime,
                                final String firstMenu,
                                final String treatMenu,
                                final String seat,
                                final String smoking,
                                final String packing,
                                final String kidsFacility,
                                final String openDateFood) {
            this("RESTAURANT", placeId, name, category, address, latitude, longitude, distanceFromStation,
                 walkTimeFromStation, stayableMinutes, visitable, stationLatitude, stationLongitude, leaveTime, images,
                 phoneNumber, restDate, useTime, null, null, null, firstMenu, treatMenu, seat, smoking, packing,
                 kidsFacility, openDateFood);
        }

        public RestaurantDetail(final String placeType,
                                final String placeId,
                                final String name,
                                final String category,
                                final String address,
                                final Double latitude,
                                final Double longitude,
                                final Integer distanceFromStation,
                                final Integer walkTimeFromStation,
                                final Integer stayableMinutes,
                                final Boolean visitable,
                                final Double stationLatitude,
                                final Double stationLongitude,
                                final LocalDateTime leaveTime,
                                final List<String> images,
                                final String phoneNumber,
                                final String restDate,
                                final String useTime,
                                final String googleOpeningStatus,
                                final LocalDateTime googleNextClosingTime,
                                final String[] googleWeekdayDescriptions,
                                final String firstMenu,
                                final String treatMenu,
                                final String seat,
                                final String smoking,
                                final String packing,
                                final String kidsFacility,
                                final String openDateFood) {
            super(placeType, placeId, name, category, address, latitude, longitude, distanceFromStation,
                  walkTimeFromStation, stayableMinutes, visitable, stationLatitude, stationLongitude, leaveTime, images,
                  phoneNumber, restDate, useTime, googleOpeningStatus, googleNextClosingTime,
                  googleWeekdayDescriptions);
            this.firstMenu = firstMenu;
            this.treatMenu = treatMenu;
            this.seat = seat;
            this.smoking = smoking;
            this.packing = packing;
            this.kidsFacility = kidsFacility;
            this.openDateFood = openDateFood;
        }

    }

    /**
     * 문화시설 상세 정보
     */
    @Getter
    @Schema(description = "문화시설 상세 정보")
    public static class CulturePlaceDetail extends PlaceDetail {

        @Schema(description = "관람소요시간", example = "2 시간")
        private final String spendTime;

        @Schema(description = "이용요금", example = "성인 5000 원")
        private final String useFee;

        @Schema(description = "할인정보", example = "청소년 50% 할인")
        private final String discountInfo;

        @Schema(description = "수용인원", example = "200")
        private final String accomCountCulture;

        @Schema(description = "주차시설", example = "가능 (최초 2 시간 무료)")
        private final String parkingCulture;

        public CulturePlaceDetail(final String placeId,
                                  final String name,
                                  final String category,
                                  final String address,
                                  final Double latitude,
                                  final Double longitude,
                                  final Integer distanceFromStation,
                                  final Integer walkTimeFromStation,
                                  final Integer stayableMinutes,
                                  final Boolean visitable,
                                  final Double stationLatitude,
                                  final Double stationLongitude,
                                  final LocalDateTime leaveTime,
                                  final List<String> images,
                                  final String phoneNumber,
                                  final String restDate,
                                  final String useTime,
                                  final String spendTime,
                                  final String useFee,
                                  final String discountInfo,
                                  final String accomCountCulture,
                                  final String parkingCulture) {
            this("CULTURE", placeId, name, category, address, latitude, longitude, distanceFromStation,
                 walkTimeFromStation, stayableMinutes, visitable, stationLatitude, stationLongitude, leaveTime, images,
                 phoneNumber, restDate, useTime, null, null, null, spendTime, useFee, discountInfo, accomCountCulture,
                 parkingCulture);
        }

        public CulturePlaceDetail(final String placeType,
                                  final String placeId,
                                  final String name,
                                  final String category,
                                  final String address,
                                  final Double latitude,
                                  final Double longitude,
                                  final Integer distanceFromStation,
                                  final Integer walkTimeFromStation,
                                  final Integer stayableMinutes,
                                  final Boolean visitable,
                                  final Double stationLatitude,
                                  final Double stationLongitude,
                                  final LocalDateTime leaveTime,
                                  final List<String> images,
                                  final String phoneNumber,
                                  final String restDate,
                                  final String useTime,
                                  final String googleOpeningStatus,
                                  final LocalDateTime googleNextClosingTime,
                                  final String[] googleWeekdayDescriptions,
                                  final String spendTime,
                                  final String useFee,
                                  final String discountInfo,
                                  final String accomCountCulture,
                                  final String parkingCulture) {
            super(placeType, placeId, name, category, address, latitude, longitude, distanceFromStation,
                  walkTimeFromStation, stayableMinutes, visitable, stationLatitude, stationLongitude, leaveTime, images,
                  phoneNumber, restDate, useTime, googleOpeningStatus, googleNextClosingTime,
                  googleWeekdayDescriptions);
            this.spendTime = spendTime;
            this.useFee = useFee;
            this.discountInfo = discountInfo;
            this.accomCountCulture = accomCountCulture;
            this.parkingCulture = parkingCulture;
        }

    }

    /**
     * 레포츠 상세 정보
     */
    @Getter
    @Schema(description = "레포츠 상세 정보")
    public static class SportsPlaceDetail extends PlaceDetail {

        @Schema(description = "개장기간", example = "2020-01-01 ~ 2020-12-31")
        private final String openPeriod;

        @Schema(description = "입장료", example = "성인 10000 원")
        private final String useFeeLeports;

        @Schema(description = "예약안내", example = "전화 예약 필요")
        private final String reservation;

        @Schema(description = "규모", example = "1000 평")
        private final String scaleLeports;

        @Schema(description = "체험가능연령", example = "12 세 이상")
        private final String expAgeRangeLeports;

        public SportsPlaceDetail(final String placeId,
                                 final String name,
                                 final String category,
                                 final String address,
                                 final Double latitude,
                                 final Double longitude,
                                 final Integer distanceFromStation,
                                 final Integer walkTimeFromStation,
                                 final Integer stayableMinutes,
                                 final Boolean visitable,
                                 final Double stationLatitude,
                                 final Double stationLongitude,
                                 final LocalDateTime leaveTime,
                                 final List<String> images,
                                 final String phoneNumber,
                                 final String restDate,
                                 final String useTime,
                                 final String openPeriod,
                                 final String useFeeLeports,
                                 final String reservation,
                                 final String scaleLeports,
                                 final String expAgeRangeLeports) {
            this("SPORTS", placeId, name, category, address, latitude, longitude, distanceFromStation,
                 walkTimeFromStation, stayableMinutes, visitable, stationLatitude, stationLongitude, leaveTime, images,
                 phoneNumber, restDate, useTime, null, null, null, openPeriod, useFeeLeports, reservation, scaleLeports,
                 expAgeRangeLeports);
        }

        public SportsPlaceDetail(final String placeType,
                                 final String placeId,
                                 final String name,
                                 final String category,
                                 final String address,
                                 final Double latitude,
                                 final Double longitude,
                                 final Integer distanceFromStation,
                                 final Integer walkTimeFromStation,
                                 final Integer stayableMinutes,
                                 final Boolean visitable,
                                 final Double stationLatitude,
                                 final Double stationLongitude,
                                 final LocalDateTime leaveTime,
                                 final List<String> images,
                                 final String phoneNumber,
                                 final String restDate,
                                 final String useTime,
                                 final String googleOpeningStatus,
                                 final LocalDateTime googleNextClosingTime,
                                 final String[] googleWeekdayDescriptions,
                                 final String openPeriod,
                                 final String useFeeLeports,
                                 final String reservation,
                                 final String scaleLeports,
                                 final String expAgeRangeLeports) {
            super(placeType, placeId, name, category, address, latitude, longitude, distanceFromStation,
                  walkTimeFromStation, stayableMinutes, visitable, stationLatitude, stationLongitude, leaveTime, images,
                  phoneNumber, restDate, useTime, googleOpeningStatus, googleNextClosingTime,
                  googleWeekdayDescriptions);
            this.openPeriod = openPeriod;
            this.useFeeLeports = useFeeLeports;
            this.reservation = reservation;
            this.scaleLeports = scaleLeports;
            this.expAgeRangeLeports = expAgeRangeLeports;
        }

    }

    /**
     * 쇼핑 상세 정보
     */
    @Getter
    @Schema(description = "쇼핑 상세 정보")
    public static class ShoppingPlaceDetail extends PlaceDetail {

        @Schema(description = "영업시간", example = "10:00 - 22:00")
        private final String openTime;

        @Schema(description = "판매품목", example = "의류, 액세서리")
        private final String saleItem;

        @Schema(description = "매장안내", example = "1 층: 의류, 2 층: 액세서리")
        private final String shopGuide;

        @Schema(description = "규모", example = "500 평")
        private final String scaleShopping;

        @Schema(description = "장서는날", example = "매월 둘째주 월요일")
        private final String fairDay;

        public ShoppingPlaceDetail(final String placeId,
                                   final String name,
                                   final String category,
                                   final String address,
                                   final Double latitude,
                                   final Double longitude,
                                   final Integer distanceFromStation,
                                   final Integer walkTimeFromStation,
                                   final Integer stayableMinutes,
                                   final Boolean visitable,
                                   final Double stationLatitude,
                                   final Double stationLongitude,
                                   final LocalDateTime leaveTime,
                                   final List<String> images,
                                   final String phoneNumber,
                                   final String restDate,
                                   final String useTime,
                                   final String openTime,
                                   final String saleItem,
                                   final String shopGuide,
                                   final String scaleShopping,
                                   final String fairDay) {
            this("SHOPPING", placeId, name, category, address, latitude, longitude, distanceFromStation,
                 walkTimeFromStation, stayableMinutes, visitable, stationLatitude, stationLongitude, leaveTime, images,
                 phoneNumber, restDate, useTime, null, null, null, openTime, saleItem, shopGuide, scaleShopping,
                 fairDay);
        }

        public ShoppingPlaceDetail(final String placeType,
                                   final String placeId,
                                   final String name,
                                   final String category,
                                   final String address,
                                   final Double latitude,
                                   final Double longitude,
                                   final Integer distanceFromStation,
                                   final Integer walkTimeFromStation,
                                   final Integer stayableMinutes,
                                   final Boolean visitable,
                                   final Double stationLatitude,
                                   final Double stationLongitude,
                                   final LocalDateTime leaveTime,
                                   final List<String> images,
                                   final String phoneNumber,
                                   final String restDate,
                                   final String useTime,
                                   final String googleOpeningStatus,
                                   final LocalDateTime googleNextClosingTime,
                                   final String[] googleWeekdayDescriptions,
                                   final String openTime,
                                   final String saleItem,
                                   final String shopGuide,
                                   final String scaleShopping,
                                   final String fairDay) {
            super(placeType, placeId, name, category, address, latitude, longitude, distanceFromStation,
                  walkTimeFromStation, stayableMinutes, visitable, stationLatitude, stationLongitude, leaveTime, images,
                  phoneNumber, restDate, useTime, googleOpeningStatus, googleNextClosingTime,
                  googleWeekdayDescriptions);
            this.openTime = openTime;
            this.saleItem = saleItem;
            this.shopGuide = shopGuide;
            this.scaleShopping = scaleShopping;
            this.fairDay = fairDay;
        }

    }

}
