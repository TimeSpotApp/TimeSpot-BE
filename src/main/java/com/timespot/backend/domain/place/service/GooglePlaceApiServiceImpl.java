package com.timespot.backend.domain.place.service;

import com.timespot.backend.domain.place.dto.GooglePlaceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PackageName : com.timespot.backend.domain.place.service
 * FileName    : GooglePlaceApiServiceImpl
 * Author      : whitecity01
 * Date        : 26. 3. 22.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 22.     whitecity01       ADD place detail
 * 26. 3. 23.     whitecity01       refactor restTemplate to restClient
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GooglePlaceApiServiceImpl implements GooglePlaceApiService {

    private final RestClient restClient;

    @Value("${google.places.api-key}")
    private String apiKey;

    private static final String GOOGLE_PLACES_API_URL = "https://places.googleapis.com/v1/places/";
    private static final String FIELD_MASK = "internationalPhoneNumber,regularOpeningHours.weekdayDescriptions,photos";
    private static final String LANGUAGE_KO = "ko";

    /**
     * 장소 상세 정보 요청 -> 정제 -> 반환
     *
     * @param googlePlaceId 장소 구글id
     * @return 구글 api 장소 세부 정보 엔티티
     */
    @Override
    public GooglePlaceDto.ParsedResult getPlaceDetails(String googlePlaceId) {
        try {
            GooglePlaceDto.Response body = restClient.get()
                    .uri(GOOGLE_PLACES_API_URL + "{placeId}", googlePlaceId)
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", FIELD_MASK)
                    .header("Accept-Language", LANGUAGE_KO)
                    .retrieve()
                    .body(GooglePlaceDto.Response.class); // 바로 객체로 매핑

            if (body == null) return createEmptyResult();

            String phoneNumber = body.getInternationalPhoneNumber();
            List<String> imageUrls = extractImageUrls(body.getPhotos());

            List<List<String>> parsedHours = parseOperatingHours(body.getRegularOpeningHours());

            return GooglePlaceDto.ParsedResult.builder()
                    .phoneNumber(phoneNumber)
                    .weekdayHours(parsedHours.get(0))
                    .weekendHours(parsedHours.get(1))
                    .imageUrls(imageUrls)
                    .build();

        } catch (Exception e) {
            log.error("Google Places API 호출 중 에러 발생: {}", e.getMessage());
            return createEmptyResult();
        }
    }

    /**
     * 최대 5개의 사진 리소스 이름을 실제 이미지 URL 리스트로 변환
     */
    private List<String> extractImageUrls(List<GooglePlaceDto.Photo> photos) {
        if (photos == null || photos.isEmpty()) return List.of();

        return photos.stream()
                .limit(5) // 최대 5개
                .map(photo -> String.format("https://places.googleapis.com/v1/%s/media?key=%s&maxWidthPx=400", photo.getName(), apiKey))
                .collect(Collectors.toList());
    }

    /**
     * 평일/주말 영업시간 분리 및 포맷팅 로직
     *
     * @param hours 영업 시간 데이터
     * @return 평일/주말 영업시간 분리 데이터
     */
    private List<List<String>> parseOperatingHours(GooglePlaceDto.RegularOpeningHours hours) {
        if (hours == null || hours.getWeekdayDescriptions() == null) {
            return List.of(List.of(), List.of());
        }

        List<String> descriptions = hours.getWeekdayDescriptions();

        // 평일 (월~금) 추출 및 "오전/오후" -> "AM/PM" 변환
        List<String> weekday = descriptions.stream()
                .filter(d -> !d.startsWith("토요일") && !d.startsWith("일요일"))
                .map(d -> d.replace("오전", "AM").replace("오후", "PM"))
                .collect(Collectors.toList());

        // 주말 (토~일) 추출 및 "오전/오후" -> "AM/PM" 변환
        List<String> weekend = descriptions.stream()
                .filter(d -> d.startsWith("토요일") || d.startsWith("일요일"))
                .map(d -> d.replace("오전", "AM").replace("오후", "PM"))
                .collect(Collectors.toList());

        return List.of(weekday, weekend);
    }

    /**
     * 비어있는 엔티티 반환
     *
     * @return 비어있는 엔티티 반환
     */
    private GooglePlaceDto.ParsedResult createEmptyResult() {
        return GooglePlaceDto.ParsedResult.builder().build();
    }
}