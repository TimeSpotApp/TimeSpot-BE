package com.timespot.backend.infra.google.places.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.infra.google.places.dto
 * FileName    : GooglePlacesResponse
 * Author      : loadingKKamo21
 * Date        : 26. 3. 30.
 * Description : Google Places API 응답 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 30.    loadingKKamo21       Initial creation
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GooglePlacesResponse {

    @JsonProperty("displayName")
    private DisplayName displayName;

    @JsonProperty("currentOpeningHours")
    private OpeningHours currentOpeningHours;

    public String getOpeningStatusKorean() {
        if (currentOpeningHours == null || currentOpeningHours.getOpenNow() == null) return null;
        return currentOpeningHours.getOpenNow() ? "영업 중" : "영업 종료";
    }

    public String[] getWeekdayDescriptions() {
        if (currentOpeningHours == null) return new String[0];
        return currentOpeningHours.getWeekdayDescriptions();
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DisplayName {

        @JsonProperty("text")
        private String text;

    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpeningHours {

        @JsonProperty("openNow")
        private Boolean openNow;

        @JsonProperty("weekdayDescriptions")
        private String[] weekdayDescriptions;

    }

}
