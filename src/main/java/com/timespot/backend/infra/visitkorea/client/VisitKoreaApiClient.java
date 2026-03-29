package com.timespot.backend.infra.visitkorea.client;

import static com.timespot.backend.common.response.ErrorCode.PLACE_API_CALL_FAILED;
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
import static java.nio.charset.StandardCharsets.UTF_8;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.infra.visitkorea.client.properties.VisitKoreaProperties;
import com.timespot.backend.infra.visitkorea.dto.VisitKoreaResponseDto.DetailInfoResponse;
import com.timespot.backend.infra.visitkorea.dto.VisitKoreaResponseDto.ImageListResponse;
import com.timespot.backend.infra.visitkorea.dto.VisitKoreaResponseDto.LocationBasedListResponse;
import com.timespot.backend.infra.visitkorea.dto.VisitKoreaResponseDto.SearchKeywordResponse;
import com.timespot.backend.infra.visitkorea.model.ContentType;
import java.net.URI;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * PackageName : com.timespot.backend.infra.visitkorea.client
 * FileName    : VisitKoreaApiClient
 * Author      : loadingKKamo21
 * Date        : 26. 3. 29.
 * Description : 한국관광공사 VisitKorea API 클라이언트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 29.    loadingKKamo21       Initial creation
 * 26. 3. 29.    loadingKKamo21       executeWithFallback 패턴 적용, baseUrl 설정, 타입별 응답 클래스 사용
 */
@Component
@Slf4j
public class VisitKoreaApiClient {

    private final RestClient           restClient;
    private final VisitKoreaProperties properties;

    public VisitKoreaApiClient(final RestClient.Builder builder,
                               final VisitKoreaProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(10).toMillis());

        this.restClient = builder.requestFactory(factory)
                                 .baseUrl(properties.getBaseUrl())
                                 .build();
        this.properties = properties;
    }

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
    public LocationBasedListResponse locationBasedList(
            final double mapX,
            final double mapY,
            final int radius,
            final ContentType contentType,
            final int pageNo,
            final int numOfRows
    ) {
        URI uri = buildLocationBasedUri(mapX, mapY, radius, contentType, pageNo, numOfRows);
        log.info("VisitKorea 위치 기반 검색 요청: uri={}", uri);

        return executeWithFallback(
                () -> restClient.get()
                                .uri(uri)
                                .retrieve()
                                .body(LocationBasedListResponse.class),
                "/locationBasedList2",
                "위치 기반 장소 검색"
        );
    }

    /**
     * 검색어 기반 관광명소 조회
     * keyword 사용하여 검색
     *
     * @param keyword   검색어 (인코딩 필요)
     * @param pageNo    페이지 번호
     * @param numOfRows 페이지당 결과 수
     * @return 검색어 기반 리스트 응답
     */
    public SearchKeywordResponse searchKeyword(
            final String keyword,
            final int pageNo,
            final int numOfRows
    ) {
        URI uri = buildSearchKeywordUri(keyword, pageNo, numOfRows);
        log.info("VisitKorea 검색어 기반 검색 요청: uri={}", uri);

        return executeWithFallback(
                () -> restClient.get()
                                .uri(uri)
                                .retrieve()
                                .body(SearchKeywordResponse.class),
                "/searchKeyword2",
                "검색어 기반 장소 검색"
        );
    }

    /**
     * 이미지 목록 조회
     * 콘텐츠 ID 기반 이미지 조회
     *
     * @param contentId 콘텐츠 ID
     * @param pageNo    페이지 번호
     * @param numOfRows 페이지당 결과 수
     * @return 이미지 리스트 응답
     */
    public ImageListResponse detailImage(
            final String contentId,
            final int pageNo,
            final int numOfRows
    ) {
        URI uri = buildDetailImageUri(contentId, pageNo, numOfRows);
        log.info("VisitKorea 이미지 조회 요청: uri={}", uri);

        return executeWithFallback(
                () -> restClient.get()
                                .uri(uri)
                                .retrieve()
                                .body(ImageListResponse.class),
                "/detailImage2",
                "장소 이미지 조회"
        );
    }

    /**
     * 장소 상세 정보 조회 (휴무일, 개장시간, 주차시설 등)
     * 콘텐츠 타입별 상이 정보 제공
     *
     * @param contentId   콘텐츠 ID
     * @param contentType 콘텐츠 타입
     * @return 상세 정보 응답
     */
    public DetailInfoResponse detailIntro(
            final String contentId,
            final ContentType contentType
    ) {
        URI uri = buildDetailIntroUri(contentId, contentType);
        log.info("VisitKorea 상세 정보 조회 요청: uri={}", uri);

        return executeWithFallback(
                () -> restClient.get()
                                .uri(uri)
                                .retrieve()
                                .body(DetailInfoResponse.class),
                "/detailIntro2",
                "장소 상세 정보 조회"
        );
    }

    // ========================= URI 빌드 메서드 =========================

    /**
     * 위치 기반 검색 URI 빌드
     */
    private URI buildLocationBasedUri(final double mapX,
                                      final double mapY,
                                      final int radius,
                                      final ContentType contentType,
                                      final int pageNo,
                                      final int numOfRows) {
        return UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                                   .path("/locationBasedList2")
                                   .queryParam(NUM_OF_ROWS, numOfRows)
                                   .queryParam(PAGE_NO, pageNo)
                                   .queryParam(MOBILE_OS, "ETC")
                                   .queryParam(MOBILE_APP, "TimeSpot")
                                   .queryParam(SERVICE_KEY, properties.getServiceKey())
                                   .queryParam(RESPONSE_TYPE, "json")
                                   .queryParam(ARRANGE, "S")
                                   .queryParam(MAP_X, mapX)
                                   .queryParam(MAP_Y, mapY)
                                   .queryParam(RADIUS, radius)
                                   .queryParam(CONTENT_TYPE_ID, contentType.getContentTypeId())
                                   .encode(UTF_8)
                                   .build()
                                   .toUri();
    }

    /**
     * 검색어 기반 검색 URI 빌드
     */
    private URI buildSearchKeywordUri(final String keyword,
                                      final int pageNo,
                                      final int numOfRows) {
        return UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                                   .path("/searchKeyword2")
                                   .queryParam(NUM_OF_ROWS, numOfRows)
                                   .queryParam(PAGE_NO, pageNo)
                                   .queryParam(MOBILE_OS, "ETC")
                                   .queryParam(MOBILE_APP, "TimeSpot")
                                   .queryParam(SERVICE_KEY, properties.getServiceKey())
                                   .queryParam(RESPONSE_TYPE, "json")
                                   .queryParam(ARRANGE, "O")
                                   .queryParam(KEYWORD, keyword)
                                   .encode(UTF_8)
                                   .build()
                                   .toUri();
    }

    /**
     * 이미지 조회 URI 빌드
     */
    private URI buildDetailImageUri(final String contentId,
                                    final int pageNo,
                                    final int numOfRows) {
        return UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                                   .path("/detailImage2")
                                   .queryParam(NUM_OF_ROWS, numOfRows)
                                   .queryParam(PAGE_NO, pageNo)
                                   .queryParam(MOBILE_OS, "ETC")
                                   .queryParam(MOBILE_APP, "TimeSpot")
                                   .queryParam(SERVICE_KEY, properties.getServiceKey())
                                   .queryParam(RESPONSE_TYPE, "json")
                                   .queryParam(CONTENT_ID, contentId)
                                   .queryParam(IMAGE_YN, "Y")
                                   .encode(UTF_8)
                                   .build()
                                   .toUri();
    }

    /**
     * 상세 정보 조회 URI 빌드
     */
    private URI buildDetailIntroUri(final String contentId,
                                    final ContentType contentType) {
        return UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                                   .path("/detailCommon")
                                   .queryParam(NUM_OF_ROWS, 1)
                                   .queryParam(PAGE_NO, 1)
                                   .queryParam(MOBILE_OS, "ETC")
                                   .queryParam(MOBILE_APP, "TimeSpot")
                                   .queryParam(SERVICE_KEY, properties.getServiceKey())
                                   .queryParam(RESPONSE_TYPE, "json")
                                   .queryParam(CONTENT_ID, contentId)
                                   .queryParam(CONTENT_TYPE_ID, contentType.getContentTypeId())
                                   .encode(UTF_8)
                                   .build()
                                   .toUri();
    }

    /**
     * 공통 예외 처리 래퍼
     */
    private <T> T executeWithFallback(final SupplierThrowing<T> supplier,
                                      final String endpoint,
                                      final String action) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("{} 실패 ({}), 원인: {}", action, endpoint, e.getMessage());
            throw new GlobalException(PLACE_API_CALL_FAILED, action + " 실패\n" + endpoint);
        }
    }

    /**
     * 예외를 던질 수 있는 Supplier 함수형 인터페이스
     */
    @FunctionalInterface
    private interface SupplierThrowing<T> {
        T get() throws Exception;
    }

}
