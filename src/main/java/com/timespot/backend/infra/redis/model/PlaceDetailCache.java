package com.timespot.backend.infra.redis.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * PackageName : com.timespot.backend.infra.redis.model
 * FileName    : PlaceDetailCache
 * Author      : loadingKKamo21
 * Date        : 26. 3. 29.
 * Description: 장소 상세 정보 캐시 모델 (타입별 필드 포함)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 29.    loadingKKamo21       Initial creation
 * 26. 3. 29.    loadingKKamo21       타입별 필드 추가
 * 26. 3. 30.    loadingKKamo21       Google Places API 정보 필드 추가
 */
@Getter
public class PlaceDetailCache {

    private final String       placeId;         // 장소 고유 식별자 (VisitKorea contentId 와 동일)
    private final String       contentTypeId;   // 콘텐츠 타입 ID (12, 14, 28, 38, 39)
    private final String       phoneNumber;     // 전화번호 (문의처)
    private final List<String> images;          // 이미지 목록
    private final String       restDate;        // 휴무일
    private final String       useTime;         // 이용시간/영업시간

    // 공통 필드 (모든 타입에서 사용)
    private final String scale;          // 규모

    // 관광지 (12) 특화 필드
    private final String openDate;          // 개장일
    private final String useSeason;         // 이용시기
    private final String expAgeRange;       // 체험가능연령
    private final String expGuide;          // 체험안내
    private final String heritage1;         // 세계문화유산
    private final String accomCount;        // 수용인원
    private final String chkBabyCarriage;   // 유모차대여
    private final String chkPet;            // 애완동물동반

    // 음식점 (39) 특화 필드
    private final String firstMenu;      // 대표메뉴
    private final String treatMenu;      // 취급메뉴
    private final String seat;           // 좌석수
    private final String smoking;        // 금연/흡연
    private final String packing;        // 포장가능
    private final String kidsFacility;   // 어린이놀이방
    private final String openDateFood;   // 개업일

    // 문화시설 (14) 특화 필드
    private final String spendTime;         // 관람소요시간
    private final String useFee;            // 이용요금
    private final String discountInfo;      // 할인정보
    private final String accomCountCulture; // 수용인원
    private final String parkingCulture;    // 주차시설

    // 레포츠 (28) 특화 필드
    private final String openPeriod;            // 개장기간
    private final String useFeeLeports;         // 입장료
    private final String reservation;           // 예약안내
    private final String scaleLeports;          // 규모
    private final String expAgeRangeLeports;    // 체험가능연령

    // 쇼핑 (38) 특화 필드
    private final String openTime;       // 영업시간
    private final String saleItem;       // 판매품목
    private final String shopGuide;      // 매장안내
    private final String scaleShopping;  // 규모
    private final String fairDay;        // 장서는날

    private final String[]      googleWeekdayDescriptions;  // 요일별 운영 시간 설명 (캐싱됨)

    @Builder
    @JsonCreator
    public PlaceDetailCache(
            @JsonProperty("placeId") final String placeId,
            @JsonProperty("contentTypeId") final String contentTypeId,
            @JsonProperty("phoneNumber") final String phoneNumber,
            @JsonProperty("images") final List<String> images,
            @JsonProperty("restDate") final String restDate,
            @JsonProperty("useTime") final String useTime,
            @JsonProperty("scale") final String scale,
            @JsonProperty("openDate") final String openDate,
            @JsonProperty("useSeason") final String useSeason,
            @JsonProperty("expAgeRange") final String expAgeRange,
            @JsonProperty("expGuide") final String expGuide,
            @JsonProperty("heritage1") final String heritage1,
            @JsonProperty("accomCount") final String accomCount,
            @JsonProperty("chkBabyCarriage") final String chkBabyCarriage,
            @JsonProperty("chkPet") final String chkPet,
            @JsonProperty("firstMenu") final String firstMenu,
            @JsonProperty("treatMenu") final String treatMenu,
            @JsonProperty("seat") final String seat,
            @JsonProperty("smoking") final String smoking,
            @JsonProperty("packing") final String packing,
            @JsonProperty("kidsFacility") final String kidsFacility,
            @JsonProperty("openDateFood") final String openDateFood,
            @JsonProperty("spendTime") final String spendTime,
            @JsonProperty("useFee") final String useFee,
            @JsonProperty("discountInfo") final String discountInfo,
            @JsonProperty("accomCountCulture") final String accomCountCulture,
            @JsonProperty("parkingCulture") final String parkingCulture,
            @JsonProperty("openPeriod") final String openPeriod,
            @JsonProperty("useFeeLeports") final String useFeeLeports,
            @JsonProperty("reservation") final String reservation,
            @JsonProperty("scaleLeports") final String scaleLeports,
            @JsonProperty("expAgeRangeLeports") final String expAgeRangeLeports,
            @JsonProperty("openTime") final String openTime,
            @JsonProperty("saleItem") final String saleItem,
            @JsonProperty("shopGuide") final String shopGuide,
            @JsonProperty("scaleShopping") final String scaleShopping,
            @JsonProperty("fairDay") final String fairDay,
            @JsonProperty("googleWeekdayDescriptions") final String[] googleWeekdayDescriptions
    ) {
        this.placeId = placeId;
        this.contentTypeId = contentTypeId;
        this.phoneNumber = phoneNumber;
        this.images = images;
        this.restDate = restDate;
        this.useTime = useTime;
        this.scale = scale;
        this.openDate = openDate;
        this.useSeason = useSeason;
        this.expAgeRange = expAgeRange;
        this.expGuide = expGuide;
        this.heritage1 = heritage1;
        this.accomCount = accomCount;
        this.chkBabyCarriage = chkBabyCarriage;
        this.chkPet = chkPet;
        this.firstMenu = firstMenu;
        this.treatMenu = treatMenu;
        this.seat = seat;
        this.smoking = smoking;
        this.packing = packing;
        this.kidsFacility = kidsFacility;
        this.openDateFood = openDateFood;
        this.spendTime = spendTime;
        this.useFee = useFee;
        this.discountInfo = discountInfo;
        this.accomCountCulture = accomCountCulture;
        this.parkingCulture = parkingCulture;
        this.openPeriod = openPeriod;
        this.useFeeLeports = useFeeLeports;
        this.reservation = reservation;
        this.scaleLeports = scaleLeports;
        this.expAgeRangeLeports = expAgeRangeLeports;
        this.openTime = openTime;
        this.saleItem = saleItem;
        this.shopGuide = shopGuide;
        this.scaleShopping = scaleShopping;
        this.fairDay = fairDay;
        this.googleWeekdayDescriptions = googleWeekdayDescriptions;
    }

    /**
     * 빈 캐시 객체 생성 (상세 정보 없음)
     */
    public static PlaceDetailCache empty() {
        return PlaceDetailCache.builder().build();
    }

    /**
     * 캐시 정보 비어있는지 확인
     */
    public boolean isEmpty() {
        return placeId == null || placeId.isBlank();
    }

}
