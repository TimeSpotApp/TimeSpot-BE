package com.timespot.backend.infra.visitkorea.dto;

import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.infra.visitkorea.dto
 * FileName    : VisitKoreaResponseDto
 * Author      : loadingKKamo21
 * Date        : 26. 3. 29.
 * Description: VisitKorea API 응답 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 29.    loadingKKamo21       Initial creation
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(access = PRIVATE)
public abstract class VisitKoreaResponseDto {

    /**
     * 장소 정보 리스트 응답
     */
    public record InfoListResponse(
            @JsonProperty("header") Header header,
            @JsonProperty("body") Body body
    ) {

        /**
         * 응답 성공 여부
         */
        public boolean isSuccess() {
            return header != null && "0000".equals(header.resultCode);
        }

    }

    /**
     * 응답 헤더
     */
    public record Header(
            @JsonProperty("resultCode") String resultCode,
            @JsonProperty("resultMsg") String resultMsg
    ) {}

    /**
     * 응답 바디
     */
    public record Body(
            @JsonProperty("items") Items items,
            @JsonProperty("numOfRows") Integer numOfRows,
            @JsonProperty("pageNo") Integer pageNo,
            @JsonProperty("totalCount") Integer totalCount
    ) {}

    /**
     * 아이템 래퍼
     */
    public record Items(
            @JsonProperty("item") List<Item> item
    ) {}

    /**
     * 장소 아이템
     */
    public record Item(
            // 필수 필드
            @JsonProperty("contentid") String contentId,
            @JsonProperty("contenttypeid") String contentTypeId,
            @JsonProperty("title") String title,

            // 주소 관련
            @JsonProperty("addr1") String addr1,
            @JsonProperty("addr2") String addr2,
            @JsonProperty("zipcode") String zipcode,
            @JsonProperty("areacode") String areaCode,
            @JsonProperty("sigungucode") String sigunguCode,

            // 카테고리 관련
            @JsonProperty("cat1") String cat1,
            @JsonProperty("cat2") String cat2,
            @JsonProperty("cat3") String cat3,

            // 좌표 관련
            @JsonProperty("mapx") Double mapX,
            @JsonProperty("mapy") Double mapY,
            @JsonProperty("mlevel") Integer mLevel,

            // 거리 (중심 좌표로부터)
            @JsonProperty("dist") Double dist,

            // 날짜 관련
            @JsonProperty("createdtime") String createdTime,
            @JsonProperty("modifiedtime") String modifiedTime,

            // 이미지 관련
            @JsonProperty("firstimage") String firstImage,
            @JsonProperty("firstimage2") String firstImage2,
            @JsonProperty("cpyrhtDivCd") String copyrightDivCd,

            // 연락처
            @JsonProperty("tel") String tel,

            // 분류체계 코드 (법정동)
            @JsonProperty("lDongRegnCd") String lDongRegnCd,
            @JsonProperty("lDongSignguCd") String lDongSignguCd,

            // 분류체계
            @JsonProperty("lclsSystm1") String lclsSystm1,
            @JsonProperty("lclsSystm2") String lclsSystm2,
            @JsonProperty("lclsSystm3") String lclsSystm3
    ) {

        /**
         * 제목 (장소명) 반환
         */
        public String getName() {
            return title;
        }

        /**
         * 전체 주소 반환
         */
        public String getFullAddress() {
            if (addr1 == null || addr1.isEmpty()) return "";
            if (addr2 == null || addr2.isEmpty()) return addr1;
            return addr1 + " " + addr2;
        }

    }

    /**
     * 이미지 리스트 응답
     */
    public record ImageListResponse(
            @JsonProperty("header") Header header,
            @JsonProperty("body") ImageBody body
    ) {

        /**
         * 응답 성공 여부
         */
        public boolean isSuccess() {
            return header != null && "0000".equals(header.resultCode);
        }

    }

    /**
     * 이미지 응답 바디
     */
    public record ImageBody(
            @JsonProperty("items") ImageItems items,
            @JsonProperty("numOfRows") Integer numOfRows,
            @JsonProperty("pageNo") Integer pageNo,
            @JsonProperty("totalCount") Integer totalCount
    ) {}

    /**
     * 이미지 아이템 래퍼
     */
    public record ImageItems(
            @JsonProperty("item") List<ImageItem> item
    ) {}

    /**
     * 이미지 정보 아이템
     */
    public record ImageItem(
            @JsonProperty("contentid") String contentId,
            @JsonProperty("originimgurl") String originImgUrl,
            @JsonProperty("smallimageurl") String smallImageUrl,
            @JsonProperty("imgname") String imgName,
            @JsonProperty("cpyrhtDivCd") String copyrightDivCd,
            @JsonProperty("serialnum") String serialNum
    ) {}

    /**
     * 장소 상세 정보 응답 (contentTypeId 별 공통 구조)
     */
    public record DetailInfoResponse(
            @JsonProperty("header") Header header,
            @JsonProperty("body") DetailInfoBody body
    ) {

        /**
         * 응답 성공 여부
         */
        public boolean isSuccess() {
            return header != null && "0000".equals(header.resultCode);
        }

    }

    /**
     * 상세 정보 응답 바디
     */
    public record DetailInfoBody(
            @JsonProperty("items") DetailInfoItems items,
            @JsonProperty("numOfRows") Integer numOfRows,
            @JsonProperty("pageNo") Integer pageNo,
            @JsonProperty("totalCount") Integer totalCount
    ) {}

    /**
     * 상세 정보 아이템 래퍼
     */
    public record DetailInfoItems(
            @JsonProperty("item") List<DetailInfoItem> item
    ) {}

    /**
     * 상세 정보 아이템 (공통 필드 + 타입별 필드)
     */
    public record DetailInfoItem(
            @JsonProperty("contentid") String contentId,
            @JsonProperty("contenttypeid") String contentTypeId,

            // ========== 공통 필드 ==========
            @JsonProperty("infocenter") String infoCenter,           // 문의및안내
            @JsonProperty("parking") String parking,                 // 주차시설
            @JsonProperty("restdate") String restDate,               // 쉬는날
            @JsonProperty("scale") String scale,                     // 규모

            // ========== 관광지 (12) ==========
            @JsonProperty("accomcount") String accomCount,           // 수용인원
            @JsonProperty("chkbabycarriage") String chkBabyCarriage, // 유모차대여
            @JsonProperty("chkcreditcard") String chkCreditCard,     // 신용카드
            @JsonProperty("chkpet") String chkPet,                   // 애완동물동반
            @JsonProperty("expagerange") String expAgeRange,         // 체험가능연령
            @JsonProperty("expguide") String expGuide,               // 체험안내
            @JsonProperty("heritage1") String heritage1,             // 세계문화유산
            @JsonProperty("heritage2") String heritage2,             // 세계자연유산
            @JsonProperty("heritage3") String heritage3,             // 세계기록유산
            @JsonProperty("opendate") String openDate,               // 개장일
            @JsonProperty("useseason") String useSeason,             // 이용시기
            @JsonProperty("usetime") String useTime,                 // 이용시간

            // ========== 문화시설 (14) ==========
            @JsonProperty("accomcountculture") String accomCountCulture,
            @JsonProperty("chkbabycarriageculture") String chkBabyCarriageCulture,
            @JsonProperty("chkcreditcardculture") String chkCreditCardCulture,
            @JsonProperty("chkpetculture") String chkPetCulture,
            @JsonProperty("discountinfo") String discountInfo,
            @JsonProperty("infocenterculture") String infoCenterCulture,
            @JsonProperty("parkingculture") String parkingCulture,
            @JsonProperty("parkingfee") String parkingFee,
            @JsonProperty("restdateculture") String restDateCulture,
            @JsonProperty("usefee") String useFee,
            @JsonProperty("usetimeculture") String useTimeCulture,
            @JsonProperty("spendtime") String spendTime,             // 관람소요시간

            // ========== 레포츠 (28) ==========
            @JsonProperty("accomcountleports") String accomCountLeports,
            @JsonProperty("chkbabycarriageleports") String chkBabyCarriageLeports,
            @JsonProperty("chkcreditcardleports") String chkCreditCardLeports,
            @JsonProperty("chkpetleports") String chkPetLeports,
            @JsonProperty("expagerangeleports") String expAgeRangeLeports,
            @JsonProperty("infocenterleports") String infoCenterLeports,
            @JsonProperty("openperiod") String openPeriod,           // 개장기간
            @JsonProperty("parkingfeeleports") String parkingFeeLeports,
            @JsonProperty("parkingleports") String parkingLeports,
            @JsonProperty("reservation") String reservation,         // 예약안내
            @JsonProperty("restdateleports") String restDateLeports,
            @JsonProperty("scaleleports") String scaleLeports,
            @JsonProperty("usefeeleports") String useFeeLeports,     // 입장료
            @JsonProperty("usetimeleports") String useTimeLeports,

            // ========== 쇼핑 (38) ==========
            @JsonProperty("chkbabycarriageshopping") String chkBabyCarriageShopping,
            @JsonProperty("chkcreditcardshopping") String chkCreditCardShopping,
            @JsonProperty("chkpetshopping") String chkPetShopping,
            @JsonProperty("culturecenter") String cultureCenter,
            @JsonProperty("fairday") String fairDay,                 // 장서는날
            @JsonProperty("infocentershopping") String infoCenterShopping,
            @JsonProperty("opendateshopping") String openDateShopping,
            @JsonProperty("opentime") String openTime,               // 영업시간
            @JsonProperty("parkingshopping") String parkingShopping,
            @JsonProperty("restdateshopping") String restDateShopping,
            @JsonProperty("restroom") String restroom,
            @JsonProperty("saleitem") String saleItem,               // 판매품목
            @JsonProperty("saleitemcost") String saleItemCost,
            @JsonProperty("scaleshopping") String scaleShopping,
            @JsonProperty("shopguide") String shopGuide,

            // ========== 음식점 (39) ==========
            @JsonProperty("chkcreditcardfood") String chkCreditCardFood,
            @JsonProperty("discountinfofood") String discountInfoFood,
            @JsonProperty("firstmenu") String firstMenu,             // 대표메뉴
            @JsonProperty("infocenterfood") String infoCenterFood,
            @JsonProperty("kidsfacility") String kidsFacility,       // 어린이놀이방
            @JsonProperty("opendatefood") String openDateFood,       // 개업일
            @JsonProperty("opentimefood") String openTimeFood,       // 영업시간
            @JsonProperty("packing") String packing,                 // 포장가능
            @JsonProperty("parkingfood") String parkingFood,
            @JsonProperty("reservationfood") String reservationFood,
            @JsonProperty("restdatefood") String restDateFood,
            @JsonProperty("scalefood") String scaleFood,
            @JsonProperty("seat") String seat,                       // 좌석수
            @JsonProperty("smoking") String smoking,                 // 금연/흡연
            @JsonProperty("treatmenu") String treatMenu,             // 취급메뉴
            @JsonProperty("lcnsno") String licenseNo                 // 인허가번호
    ) {}

}
