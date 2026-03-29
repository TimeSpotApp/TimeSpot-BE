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

}
