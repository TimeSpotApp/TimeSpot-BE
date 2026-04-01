package com.timespot.backend.infra.visitkorea.constant;

import static lombok.AccessLevel.PRIVATE;

import java.time.Duration;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.infra.visitkorea.constant
 * FileName    : VisitKoreaConst
 * Author      : loadingKKamo21
 * Date        : 26. 3. 29.
 * Description : VisitKorea API 상수 정의
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 29.    loadingKKamo21       Initial creation
 * 26. 4. 1.     loadingKKamo21       캐시 TTL, Redis 키 접두사 추가
 */
@NoArgsConstructor(access = PRIVATE)
public abstract class VisitKoreaConst {

    // ======================= 캐시 TTL =======================

    public static final Duration PLACE_CARD_CACHE_TTL   = Duration.ofHours(24);
    public static final Duration PLACE_DETAIL_CACHE_TTL = Duration.ofDays(7);

    // ======================= Redis 키 접두사 =======================

    public static final String GEO_KEY_PREFIX          = "place:geo:";
    public static final String CARD_CACHE_KEY_PREFIX   = "place:card:";
    public static final String DETAIL_CACHE_KEY_PREFIX = "place:detail:";

    // ======================= API 기본 설정 =======================

    public static final int    MAX_RADIUS_METERS = 20000;   // 최대 검색 반경 (20km)
    public static final int    DEFAULT_PAGE_SIZE = 100;     // 페이지당 결과 수
    public static final String SORT_BY_DISTANCE  = "A";     // 거리순 정렬

    // ======================= 공통 파라미터 =======================

    public static final String SERVICE_KEY   = "ServiceKey";    // 서비스 키 (인증키)
    public static final String NUM_OF_ROWS   = "numOfRows";     // 한 페이지 결과 수
    public static final String PAGE_NO       = "pageNo";        // 페이지 번호
    public static final String MOBILE_OS     = "MobileOS";      // OS 구분 (IOS, AND, WEB, ETC)
    public static final String MOBILE_APP    = "MobileApp";     // 서비스명 (어플명)
    public static final String RESPONSE_TYPE = "_type";         // 응답 메세지 형식 (json, xml)
    public static final String ARRANGE       = "arrange";       // 정렬 구분 - A: 제목순, C: 수정일순, D: 생성일순, E: 거리순, O: 제목순, Q: 수정일순, R: 생성일순, S: 거리순

    // ======================= 위치 기반 검색 (locationBasedList2) =======================

    public static final String MAP_X           = "mapX";            // X 좌표 (경도, WGS84)
    public static final String MAP_Y           = "mapY";            // Y 좌표 (위도, WGS84)
    public static final String RADIUS          = "radius";          // 거리 반경 (단위: m, 최대 20000)
    public static final String CONTENT_TYPE_ID = "contentTypeId";   // 관광타입 ID - 12: 관광지, 14: 문화시설, 15: 축제공연행사, 25: 여행코스, 28: 레포츠, 32: 숙박, 38: 쇼핑, 39: 음식점
    public static final String MODIFIED_TIME   = "modifiedtime";    // 콘텐츠 수정일 (형식: YYYYMMDD)

    // ======================= 검색어 기반 검색 (searchKeyword2) =======================

    public static final String KEYWORD   = "keyword";   // 검색어 (URLEncoder 인코딩 필수)
    public static final String AREA_CODE = "areaCode";  // 지역 코드 (시도 코드, 선택적)

    // ======================= 이미지 조회 (detailImage2) =======================

    public static final String CONTENT_ID = "contentId";    // 콘텐츠 ID
    public static final String IMAGE_YN   = "imageYN";      // 이미지 여부 (Y: 이미지 있는 것만, N: 전체)

    // ======================= 카테고리 매핑 =======================

    /**
     * 콘텐츠 타입 ID 를 카테고리명으로 매핑
     *
     * @param contentTypeId 콘텐츠 타입 ID
     * @return 카테고리명
     */
    public static String mapContentTypeIdToCategory(final String contentTypeId) {
        return switch (contentTypeId) {
            case "12" -> "관광지";
            case "14" -> "문화시설";
            case "28" -> "레포츠";
            case "38" -> "쇼핑";
            case "39" -> "음식점";
            default -> "기타";
        };
    }

    /**
     * 카테고리명을 콘텐츠 타입 ID 로 변환
     *
     * @param category 카테고리명
     * @return 콘텐츠 타입 ID
     */
    public static String mapCategoryToContentTypeId(final String category) {
        return switch (category) {
            case "관광지" -> "12";
            case "문화시설" -> "14";
            case "레포츠" -> "28";
            case "쇼핑" -> "38";
            case "음식점" -> "39";
            default -> "12";
        };
    }

}
