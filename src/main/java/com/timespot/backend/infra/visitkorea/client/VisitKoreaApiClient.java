package com.timespot.backend.infra.visitkorea.client;

import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.AREA_CODE;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.ARRANGE;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.CONTENT_ID;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.CONTENT_TYPE_ID;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.IMAGE_YN;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.KEYWORD;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.MAP_X;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.MAP_Y;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.MOBILE_APP;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.MOBILE_OS;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.NUM_OF_ROWS;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.PAGE_NO;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.RADIUS;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.RESPONSE_TYPE;
import static com.timespot.backend.infra.visitkorea.constant.VisitKoreaConst.SERVICE_KEY;

import com.timespot.backend.infra.visitkorea.client.properties.VisitKoreaProperties;
import com.timespot.backend.infra.visitkorea.dto.VisitKoreaResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * PackageName : com.timespot.backend.infra.visitkorea.client
 * FileName    : VisitKoreaApiClient
 * Author      : loadingKKamo21
 * Date        : 26. 3. 29.
 * Description: 한국관광공사 VisitKorea API 클라이언트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 29.    loadingKKamo21       Initial creation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VisitKoreaApiClient {

    private final RestClient           restClient;
    private final VisitKoreaProperties visitKoreaProperties;

    /**
     * 위치 기반 관광명소 조회
     * - mapX, mapY, radius 사용하여 좌표 주변 검색
     *
     * @param mapX          경도 (longitude)
     * @param mapY          위도 (latitude)
     * @param radius        반경 (미터, 최대 20000)
     * @param contentTypeId 콘텐츠 타입 (12: 관광지, 14: 문화시설, 28: 레포츠, 39: 음식점)
     * @param pageNo        페이지 번호 (1-based)
     * @param numOfRows     페이지당 결과 수 (최대 100)
     * @return 위치 기반 리스트 응답
     */
    public VisitKoreaResponseDto.InfoListResponse locationBasedList(
            final double mapX,
            final double mapY,
            final int radius,
            final String contentTypeId,
            final int pageNo,
            final int numOfRows
    ) {
        log.debug("VisitKorea 위치 기반 검색: mapX={}, mapY={}, radius={}, typeId={}, page={}",
                  mapX, mapY, radius, contentTypeId, pageNo);
        return restClient.get()
                         .uri(uriBuilder -> uriBuilder
                                 .path("/B551011/KorService2/locationBasedList2")
                                 .queryParam(SERVICE_KEY, visitKoreaProperties.getServiceKeyEncoded())
                                 .queryParam(MAP_X, mapX)
                                 .queryParam(MAP_Y, mapY)
                                 .queryParam(RADIUS, radius)
                                 .queryParam(CONTENT_TYPE_ID, contentTypeId)
                                 .queryParam(MOBILE_OS, "ETC")
                                 .queryParam(MOBILE_APP, "TimeSpot-BE")
                                 .queryParam(ARRANGE, "A")  // 거리순 정렬
                                 .queryParam(NUM_OF_ROWS, numOfRows)
                                 .queryParam(PAGE_NO, pageNo)
                                 .queryParam(RESPONSE_TYPE, "json")  // JSON 응답
                                 .build())
                         .retrieve()
                         .body(VisitKoreaResponseDto.InfoListResponse.class);
    }

    /**
     * 검색어 기반 관광명소 조회
     * - keyword 사용하여 검색
     *
     * @param keyword       검색어
     * @param areaCode      지역 코드 (선택, null 가능)
     * @param contentTypeId 콘텐츠 타입 (선택, null 가능)
     * @param pageNo        페이지 번호
     * @param numOfRows     페이지당 결과 수
     * @return 검색어 기반 리스트 응답
     */
    public VisitKoreaResponseDto.InfoListResponse searchKeyword(
            final String keyword,
            final String areaCode,
            final String contentTypeId,
            final int pageNo,
            final int numOfRows
    ) {
        log.debug("VisitKorea 검색어 기반 검색: keyword={}, areaCode={}, typeId={}",
                  keyword, areaCode, contentTypeId);
        return restClient.get()
                         .uri(uriBuilder -> uriBuilder
                                 .path("/B551011/KorService2/searchKeyword2")
                                 .queryParam(SERVICE_KEY, visitKoreaProperties.getServiceKeyEncoded())
                                 .queryParam(KEYWORD, keyword)
                                 .queryParam(AREA_CODE, areaCode != null ? areaCode : "")
                                 .queryParam(CONTENT_TYPE_ID, contentTypeId != null ? contentTypeId : "")
                                 .queryParam(MOBILE_OS, "ETC")
                                 .queryParam(MOBILE_APP, "TimeSpot-BE")
                                 .queryParam(ARRANGE, "A")
                                 .queryParam(NUM_OF_ROWS, numOfRows)
                                 .queryParam(PAGE_NO, pageNo)
                                 .queryParam(RESPONSE_TYPE, "json")
                                 .build())
                         .retrieve()
                         .body(VisitKoreaResponseDto.InfoListResponse.class);
    }

    /**
     * 이미지 목록 조회
     * - 콘텐츠 ID 기반 이미지 조회
     *
     * @param contentId 콘텐츠 ID
     * @param pageNo    페이지 번호
     * @param numOfRows 페이지당 결과 수
     * @return 이미지 리스트 응답
     */
    public VisitKoreaResponseDto.ImageListResponse detailImage(
            final String contentId,
            final int pageNo,
            final int numOfRows
    ) {
        log.debug("VisitKorea 이미지 조회: contentId={}, page={}", contentId, pageNo);
        return restClient.get()
                         .uri(uriBuilder -> uriBuilder
                                 .path("/B551011/KorService2/detailImage2")
                                 .queryParam(SERVICE_KEY, visitKoreaProperties.getServiceKeyEncoded())
                                 .queryParam(CONTENT_ID, contentId)
                                 .queryParam(MOBILE_OS, "ETC")
                                 .queryParam(MOBILE_APP, "TimeSpot-BE")
                                 .queryParam(IMAGE_YN, "Y")
                                 .queryParam(NUM_OF_ROWS, numOfRows)
                                 .queryParam(PAGE_NO, pageNo)
                                 .queryParam(RESPONSE_TYPE, "json")
                                 .build())
                         .retrieve()
                         .body(VisitKoreaResponseDto.ImageListResponse.class);
    }

    /**
     * 최대 검색 반경 반환 (서비스 정책)
     */
    public int getMaxRadiusMeters() {
        return visitKoreaProperties.getMaxRadiusMeters();
    }

    /**
     * 기본 검색 반경 반환
     */
    public int getDefaultRadiusMeters() {
        return visitKoreaProperties.getDefaultRadiusMeters();
    }

}
