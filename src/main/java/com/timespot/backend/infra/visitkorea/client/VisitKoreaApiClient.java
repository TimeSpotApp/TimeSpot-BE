package com.timespot.backend.infra.visitkorea.client;

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
import com.timespot.backend.infra.visitkorea.model.ContentType;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

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
     * mapX, mapY, radius 사용하여 좌표 주변 검색
     *
     * @param mapX        경도 (longitude)
     * @param mapY        위도 (latitude)
     * @param radius      반경 (미터, 최대 20000)
     * @param contentType 콘텐츠 타입
     * @param pageNo      페이지 번호 (1-based)
     * @param numOfRows   페이지당 결과 수 (최대 100)
     * @return 위치 기반 리스트 응답
     */
    public VisitKoreaResponseDto.InfoListResponse locationBasedList(
            final double mapX,
            final double mapY,
            final int radius,
            final ContentType contentType,
            final int pageNo,
            final int numOfRows
    ) {
        log.debug("VisitKorea 위치 기반 검색: mapX={}, mapY={}, radius={}, typeId={}, page={}",
                  mapX, mapY, radius, contentType.getContentTypeId(), pageNo);
        return restClient.get()
                         .uri(uriBuilder -> {
                             UriBuilder builder = uriBuilder
                                     .path("/locationBasedList2")
                                     .queryParam(NUM_OF_ROWS, numOfRows)
                                     .queryParam(PAGE_NO, pageNo)
                                     .queryParam(MOBILE_OS, "ETC")
                                     .queryParam(MOBILE_APP, "TimeSpot")
                                     .queryParam(SERVICE_KEY, visitKoreaProperties.getServiceKeyEncoded())
                                     .queryParam(RESPONSE_TYPE, "json")
                                     .queryParam(ARRANGE, "S")
                                     .queryParam(MAP_X, mapX)
                                     .queryParam(MAP_Y, mapY)
                                     .queryParam(RADIUS, radius);
                             if (contentType.getContentTypeId() != null)
                                 builder.queryParam(CONTENT_TYPE_ID, contentType.getContentTypeId());
                             return builder.build();
                         })
                         .retrieve()
                         .body(VisitKoreaResponseDto.InfoListResponse.class);
    }

    /**
     * 검색어 기반 관광명소 조회
     * keyword 사용하여 검색(URLEncoder 인코딩 필수)
     *
     * @param keyword   검색어 (인코딩 필요)
     * @param areaCode  지역 코드 (선택, null 가능)
     * @param pageNo    페이지 번호
     * @param numOfRows 페이지당 결과 수
     * @return 검색어 기반 리스트 응답
     */
    public VisitKoreaResponseDto.InfoListResponse searchKeyword(
            final String keyword,
            final String areaCode,
            final int pageNo,
            final int numOfRows
    ) {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        log.debug("VisitKorea 검색어 기반 검색: keyword={}, encodedKeyword={}, areaCode={}", keyword, encodedKeyword, areaCode);
        return restClient.get()
                         .uri(uriBuilder -> uriBuilder
                                 .path("/searchKeyword2")
                                 .queryParam(NUM_OF_ROWS, numOfRows)
                                 .queryParam(PAGE_NO, pageNo)
                                 .queryParam(MOBILE_OS, "ETC")
                                 .queryParam(MOBILE_APP, "TimeSpot")
                                 .queryParam(SERVICE_KEY, visitKoreaProperties.getServiceKeyEncoded())
                                 .queryParam(RESPONSE_TYPE, "json")
                                 .queryParam(ARRANGE, "O")
                                 .queryParam(KEYWORD, encodedKeyword).build())
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
                                 .path("/detailImage2")
                                 .queryParam(NUM_OF_ROWS, numOfRows)
                                 .queryParam(PAGE_NO, pageNo)
                                 .queryParam(MOBILE_OS, "ETC")
                                 .queryParam(MOBILE_APP, "TimeSpot")
                                 .queryParam(SERVICE_KEY, visitKoreaProperties.getServiceKeyEncoded())
                                 .queryParam(RESPONSE_TYPE, "json")
                                 .queryParam(CONTENT_ID, contentId)
                                 .queryParam(IMAGE_YN, "Y")
                                 .build())
                         .retrieve()
                         .body(VisitKoreaResponseDto.ImageListResponse.class);
    }

    /**
     * 장소 상세 정보 조회 (휴무일, 개장시간, 주차시설 등)
     * 콘텐츠 타입별 상이 정보 제공
     *
     * @param contentId   콘텐츠 ID
     * @param contentType 콘텐츠 타입
     * @return 상세 정보 응답
     */
    public VisitKoreaResponseDto.DetailInfoResponse detailIntro(
            final String contentId,
            final ContentType contentType
    ) {
        log.debug("VisitKorea 상세 정보 조회: contentId={}, typeId={}", contentId, contentType.getContentTypeId());
        return restClient.get()
                         .uri(uriBuilder -> uriBuilder
                                 .path("/detailIntro2")
                                 .queryParam(NUM_OF_ROWS, 1)
                                 .queryParam(PAGE_NO, 1)
                                 .queryParam(MOBILE_OS, "ETC")
                                 .queryParam(MOBILE_APP, "TimeSpot")
                                 .queryParam(SERVICE_KEY, visitKoreaProperties.getServiceKeyEncoded())
                                 .queryParam(RESPONSE_TYPE, "json")
                                 .queryParam(CONTENT_ID, contentId)
                                 .queryParam(CONTENT_TYPE_ID, contentType.getContentTypeId())
                                 .build())
                         .retrieve()
                         .body(VisitKoreaResponseDto.DetailInfoResponse.class);
    }

}
