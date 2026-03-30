package com.timespot.backend.infra.google.places.client;

import static com.timespot.backend.common.response.ErrorCode.PLACE_API_CALL_FAILED;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.infra.google.places.client.properties.GooglePlacesProperties;
import com.timespot.backend.infra.google.places.dto.GooglePlacesResponse;
import com.timespot.backend.infra.google.places.dto.GooglePlacesSearchRequest;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * PackageName : com.timespot.backend.infra.google.places.client
 * FileName    : GooglePlacesApiClient
 * Author      : loadingKKamo21
 * Date        : 26. 3. 30.
 * Description : Google Places API 클라이언트
 * <p>
 * Google Places API Text Search 를 제공합니다.
 * 장소명과 좌표를 사용하여 반경 내의 장소를 검색합니다.
 * </p>
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 30.    loadingKKamo21       Initial creation
 */
@Component
@Slf4j
public class GooglePlacesApiClient {

    private static final String TEXT_SEARCH_ENDPOINT = "/v1/places:searchText";

    private final RestClient             restClient;
    private final GooglePlacesProperties properties;
    private final ObjectMapper           objectMapper;

    public GooglePlacesApiClient(final RestClient.Builder builder,
                                 final GooglePlacesProperties properties,
                                 final ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(10).toMillis());

        this.restClient = builder.requestFactory(factory)
                                 .baseUrl(properties.getBaseUrl())
                                 .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                 .defaultHeader("X-Goog-Api-Key", properties.getApiKey())
                                 .defaultHeader("X-Goog-FieldMask",
                                                "id,displayName,currentOpeningStatus,openingHours,nextClosingTime")
                                 .build();
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 장소 이름과 좌표로 Google Place 검색
     * <p>
     * VisitKorea 데이터와 매핑하기 위해 다음 정보를 사용합니다:
     * - 장소명 (textQuery)
     * - 위도/경도 (locationBias)
     * - 반경 50m (radius)
     * </p>
     *
     * @param placeName 장소 이름
     * @param latitude  위도
     * @param longitude 경도
     * @return 검색된 Google Place 응답 목록 (최대 3 개)
     */
    public List<GooglePlacesResponse> searchByPlaceNameAndLocation(final String placeName,
                                                                   final double latitude,
                                                                   final double longitude) {
        String query = buildSearchQuery(placeName);

        GooglePlacesSearchRequest request = GooglePlacesSearchRequest.builder()
                                                                     .textQuery(query)
                                                                     .latitude(latitude)
                                                                     .longitude(longitude)
                                                                     .radius(properties.getSearchRadiusMeters())
                                                                     .languageCode("ko")
                                                                     .regionCode("KR")
                                                                     .build();

        log.info("Google Places 검색 요청: query={}, location=({}, {}), radius={}m",
                 query, latitude, longitude, properties.getSearchRadiusMeters());

        try {
            String responseJson = restClient.post()
                                            .uri(TEXT_SEARCH_ENDPOINT)
                                            .body(request)
                                            .retrieve()
                                            .body(String.class);

            JsonNode rootNode   = objectMapper.readTree(responseJson);
            JsonNode placesNode = rootNode.path("places");

            if (placesNode.isEmpty()) {
                log.warn("Google Places 검색 결과 없음: query={}", query);
                return Collections.emptyList();
            }

            return objectMapper.convertValue(
                    placesNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, GooglePlacesResponse.class)
            );
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google Places API 호출 실패: query={}, error={}", query, e.getMessage());
            throw new GlobalException(PLACE_API_CALL_FAILED, "Google Places 검색 실패: " + e.getMessage());
        }
    }

    /**
     * 검색 쿼리 빌드
     *
     * @param placeName 장소 이름
     * @return 검색 쿼리 문자열
     */
    private String buildSearchQuery(final String placeName) {
        return placeName.trim();
    }

}
