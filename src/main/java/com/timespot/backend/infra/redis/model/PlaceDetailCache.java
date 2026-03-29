package com.timespot.backend.infra.redis.model;

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

    @Builder
    public PlaceDetailCache(final String placeId,
                            final String contentTypeId,
                            final String phoneNumber,
                            final List<String> images,
                            final String restDate,
                            final String useTime,
                            final String scale,
                            final String openDate,
                            final String useSeason,
                            final String expAgeRange,
                            final String expGuide,
                            final String heritage1,
                            final String accomCount,
                            final String chkBabyCarriage,
                            final String chkPet,
                            final String firstMenu,
                            final String treatMenu,
                            final String seat,
                            final String smoking,
                            final String packing,
                            final String kidsFacility,
                            final String openDateFood,
                            final String spendTime,
                            final String useFee,
                            final String discountInfo,
                            final String accomCountCulture,
                            final String parkingCulture,
                            final String openPeriod,
                            final String useFeeLeports,
                            final String reservation,
                            final String scaleLeports,
                            final String expAgeRangeLeports,
                            final String openTime,
                            final String saleItem,
                            final String shopGuide,
                            final String scaleShopping,
                            final String fairDay) {
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
