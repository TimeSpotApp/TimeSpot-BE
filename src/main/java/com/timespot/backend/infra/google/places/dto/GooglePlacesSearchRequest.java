package com.timespot.backend.infra.google.places.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * PackageName : com.timespot.backend.infra.google.places.dto
 * FileName    : GooglePlacesSearchRequest
 * Author      : loadingKKamo21
 * Date        : 26. 3. 30.
 * Description : Google Places Text Search 요청 DTO
 * <p>
 * 장소 이름과 좌표를 사용하여 장소를 검색하기 위한 요청
 * </p>
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 30.    loadingKKamo21       Initial creation
 */
@Getter
@JsonInclude(NON_NULL)
public class GooglePlacesSearchRequest {

    @JsonProperty("textQuery")
    private final String textQuery;

    @JsonProperty("locationBias")
    private final LocationBias locationBias;

    @JsonProperty("languageCode")
    private final String languageCode;

    @JsonProperty("regionCode")
    private final String regionCode;

    @Builder
    public GooglePlacesSearchRequest(final String textQuery,
                                     final double latitude,
                                     final double longitude,
                                     final int radius,
                                     final String languageCode,
                                     final String regionCode) {
        this.textQuery = textQuery;
        this.locationBias = new LocationBias(latitude, longitude, radius);
        this.languageCode = languageCode != null ? languageCode : "ko";
        this.regionCode = regionCode != null ? regionCode : "KR";
    }

    @Getter
    @JsonInclude(NON_NULL)
    public static class LocationBias {

        @JsonProperty("circle")
        private final Circle circle;

        public LocationBias(final double latitude, final double longitude, final int radius) {
            this.circle = new Circle(latitude, longitude, radius);
        }

    }

    @Getter
    @JsonInclude(NON_NULL)
    public static class Circle {

        @JsonProperty("center")
        private final Center center;

        @JsonProperty("radius")
        private final int radius;

        public Circle(final double latitude, final double longitude, final int radius) {
            this.center = new Center(latitude, longitude);
            this.radius = radius;
        }

    }

    @Getter
    @JsonInclude(NON_NULL)
    public static class Center {

        @JsonProperty("latitude")
        private final double latitude;

        @JsonProperty("longitude")
        private final double longitude;

        public Center(final double latitude, final double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

    }

}
