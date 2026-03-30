package com.timespot.backend.infra.visitkorea.dto;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@NoArgsConstructor(access = PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class VisitKoreaResponseDto {

    /**
     * 최상위 래퍼 클래스
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VisitKoreaEnvelope {

        @JsonProperty("response")
        private LocationBasedListResponse response;

    }

    /**
     * 응답 헤더 (공통)
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {

        @JsonProperty("resultCode")
        private String resultCode;

        @JsonProperty("resultMsg")
        private String resultMsg;

    }

    /**
     * 위치 기반 검색 응답 (locationBasedList2)
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LocationBasedListResponse {

        @JsonProperty("response")
        private ResponseWrapper response;

        /**
         * 응답 성공 여부
         */
        public boolean isSuccess() {
            return response != null
                   && response.getHeader() != null
                   && "0000".equals(response.getHeader().getResultCode());
        }

        /**
         * 바디 직접 접근
         */
        public LocationBasedListBody getBody() {
            return response != null ? response.getBody() : null;
        }

    }

    /**
     * 실제 API 응답 래퍼
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseWrapper {

        @JsonProperty("header")
        private Header header;

        @JsonProperty("body")
        private LocationBasedListBody body;

    }

    /**
     * 위치 기반 검색 응답 바디
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LocationBasedListBody {

        @JsonProperty("items")
        private LocationBasedListItems items;

        @JsonProperty("numOfRows")
        private Integer numOfRows;

        @JsonProperty("pageNo")
        private Integer pageNo;

        @JsonProperty("totalCount")
        private Integer totalCount;

    }

    /**
     * 위치 기반 검색 아이템 리스트
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LocationBasedListItems {

        @JsonProperty("item")
        private List<LocationBasedListItem> item;

    }

    /**
     * 위치 기반 검색 장소 아이템
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LocationBasedListItem {

        @JsonProperty("contentid")
        private String contentId;

        @JsonProperty("contenttypeid")
        private String contentTypeId;

        @JsonProperty("title")
        private String title;

        @JsonProperty("addr1")
        private String addr1;

        @JsonProperty("addr2")
        private String addr2;

        @JsonProperty("cat1")
        private String cat1;

        @JsonProperty("cat2")
        private String cat2;

        @JsonProperty("cat3")
        private String cat3;

        @JsonProperty("mapx")
        private Double mapX;

        @JsonProperty("mapy")
        private Double mapY;

        @JsonProperty("dist")
        private Double dist;

        @JsonProperty("firstimage")
        private String firstImage;

        @JsonProperty("tel")
        private String tel;

        /**
         * 장소명 반환
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
     * 검색어 기반 검색 응답 (searchKeyword2)
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchKeywordResponse {

        @JsonProperty("response")
        private SearchKeywordResponseWrapper response;

        /**
         * 응답 성공 여부
         */
        public boolean isSuccess() {
            return response != null
                   && response.getHeader() != null
                   && "0000".equals(response.getHeader().getResultCode());
        }

        /**
         * 바디 직접 접근 (기존 코드 호환성)
         */
        public SearchKeywordBody getBody() {
            return response != null ? response.getBody() : null;
        }

    }

    /**
     * 검색어 기반 검색 응답 래퍼
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchKeywordResponseWrapper {

        @JsonProperty("header")
        private Header header;

        @JsonProperty("body")
        private SearchKeywordBody body;

    }

    /**
     * 검색어 기반 검색 응답 바디
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchKeywordBody {

        @JsonProperty("items")
        private SearchKeywordItems items;

        @JsonProperty("numOfRows")
        private Integer numOfRows;

        @JsonProperty("pageNo")
        private Integer pageNo;

        @JsonProperty("totalCount")
        private Integer totalCount;

    }

    /**
     * 검색어 기반 검색 아이템 리스트
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchKeywordItems {

        @JsonProperty("item")
        private List<SearchKeywordItem> item;

    }

    /**
     * 검색어 기반 검색 장소 아이템
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchKeywordItem {

        @JsonProperty("contentid")
        private String contentId;

        @JsonProperty("contenttypeid")
        private String contentTypeId;

        @JsonProperty("title")
        private String title;

        @JsonProperty("addr1")
        private String addr1;

        @JsonProperty("addr2")
        private String addr2;

        @JsonProperty("cat1")
        private String cat1;

        @JsonProperty("mapx")
        private Double mapX;

        @JsonProperty("mapy")
        private Double mapY;

        @JsonProperty("firstimage")
        private String firstImage;

        @JsonProperty("tel")
        private String tel;

        /**
         * 장소명 반환
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
     * 이미지 리스트 응답 (detailImage2)
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageListResponse {

        @JsonProperty("response")
        private ImageListResponseWrapper response;

        /**
         * 응답 성공 여부
         */
        public boolean isSuccess() {
            return response != null
                   && response.getHeader() != null
                   && "0000".equals(response.getHeader().getResultCode());
        }

        /**
         * 바디 직접 접근 (기존 코드 호환성)
         */
        public ImageListBody getBody() {
            return response != null ? response.getBody() : null;
        }

    }

    /**
     * 이미지 리스트 응답 래퍼
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageListResponseWrapper {

        @JsonProperty("header")
        private Header header;

        @JsonProperty("body")
        private ImageListBody body;

    }

    /**
     * 이미지 리스트 응답 바디
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageListBody {

        @JsonProperty("items")
        private ImageListItems items;

        @JsonProperty("numOfRows")
        private Integer numOfRows;

        @JsonProperty("pageNo")
        private Integer pageNo;

        @JsonProperty("totalCount")
        private Integer totalCount;

    }

    /**
     * 이미지 리스트 아이템
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageListItems {

        @JsonProperty("item")
        private List<ImageListItem> item;

    }

    /**
     * 이미지 아이템
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageListItem {

        @JsonProperty("contentid")
        private String contentId;

        @JsonProperty("originimgurl")
        private String originImgUrl;

        @JsonProperty("smallimageurl")
        private String smallImageUrl;

        @JsonProperty("imgname")
        private String imgName;

    }

    /**
     * 장소 상세 정보 응답 (detailIntro2)
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetailInfoResponse {

        @JsonProperty("response")
        private DetailInfoResponseWrapper response;

        /**
         * 응답 성공 여부
         */
        public boolean isSuccess() {
            return response != null
                   && response.getHeader() != null
                   && "0000".equals(response.getHeader().getResultCode());
        }

        /**
         * 바디 직접 접근 (기존 코드 호환성)
         */
        public DetailInfoBody getBody() {
            return response != null ? response.getBody() : null;
        }

    }

    /**
     * 상세 정보 응답 래퍼
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetailInfoResponseWrapper {

        @JsonProperty("header")
        private Header header;

        @JsonProperty("body")
        private DetailInfoBody body;

    }

    /**
     * 상세 정보 응답 바디
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetailInfoBody {

        @JsonProperty("items")
        private DetailInfoItems items;

        @JsonProperty("numOfRows")
        private Integer numOfRows;

        @JsonProperty("pageNo")
        private Integer pageNo;

        @JsonProperty("totalCount")
        private Integer totalCount;

    }

    /**
     * 상세 정보 아이템 리스트
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetailInfoItems {

        @JsonProperty("item")
        private List<DetailInfoItem> item;

    }

    /**
     * 상세 정보 아이템 (공통 + 타입별 필드)
     */
    @Getter
    @Setter
    @NoArgsConstructor(access = PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetailInfoItem {

        @JsonProperty("contentid")
        private String contentId;

        @JsonProperty("contenttypeid")
        private String contentTypeId;

        // ========== 공통 필드 ==========

        @JsonProperty("infocenter")
        private String infoCenter;

        @JsonProperty("parking")
        private String parking;

        @JsonProperty("restdate")
        private String restDate;

        @JsonProperty("scale")
        private String scale;

        // ========== 관광지 (12) ==========

        @JsonProperty("accomcount")
        private String accomCount;

        @JsonProperty("chkbabycarriage")
        private String chkBabyCarriage;

        @JsonProperty("chkpet")
        private String chkPet;

        @JsonProperty("expagerange")
        private String expAgeRange;

        @JsonProperty("expguide")
        private String expGuide;

        @JsonProperty("heritage1")
        private String heritage1;

        @JsonProperty("opendate")
        private String openDate;

        @JsonProperty("useseason")
        private String useSeason;

        @JsonProperty("usetime")
        private String useTime;

        // ========== 문화시설 (14) ==========

        @JsonProperty("spendtime")
        private String spendTime;

        @JsonProperty("usefee")
        private String useFee;

        @JsonProperty("discountinfo")
        private String discountInfo;

        @JsonProperty("accomcountculture")
        private String accomCountCulture;

        @JsonProperty("parkingculture")
        private String parkingCulture;

        @JsonProperty("usetimeculture")
        private String useTimeCulture;

        // ========== 레포츠 (28) ==========

        @JsonProperty("openperiod")
        private String openPeriod;

        @JsonProperty("usefeeleports")
        private String useFeeLeports;

        @JsonProperty("reservation")
        private String reservation;

        @JsonProperty("scaleleports")
        private String scaleLeports;

        @JsonProperty("expagerangeleports")
        private String expAgeRangeLeports;

        @JsonProperty("usetimeleports")
        private String useTimeLeports;

        // ========== 쇼핑 (38) ==========

        @JsonProperty("opentime")
        private String openTime;

        @JsonProperty("saleitem")
        private String saleItem;

        @JsonProperty("shopguide")
        private String shopGuide;

        @JsonProperty("scaleshopping")
        private String scaleShopping;

        @JsonProperty("fairday")
        private String fairDay;

        @JsonProperty("opendateshopping")
        private String openDateShopping;

        // ========== 음식점 (39) ==========

        @JsonProperty("firstmenu")
        private String firstMenu;

        @JsonProperty("treatmenu")
        private String treatMenu;

        @JsonProperty("seat")
        private String seat;

        @JsonProperty("smoking")
        private String smoking;

        @JsonProperty("packing")
        private String packing;

        @JsonProperty("kidsfacility")
        private String kidsFacility;

        @JsonProperty("opendatefood")
        private String openDateFood;

        @JsonProperty("opentimefood")
        private String openTimeFood;

    }

}
