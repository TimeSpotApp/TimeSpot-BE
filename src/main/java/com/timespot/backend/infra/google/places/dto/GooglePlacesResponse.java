package com.timespot.backend.infra.google.places.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    @JsonProperty("id")
    private String id;

    @JsonProperty("displayName")
    private DisplayName displayName;

    @JsonProperty("currentOpeningStatus")
    private String currentOpeningStatus;

    @JsonProperty("openingHours")
    private OpeningHours openingHours;

    @JsonProperty("nextClosingTime")
    private NextClosingTime nextClosingTime;

    public String getOpeningStatusKorean() {
        if (currentOpeningStatus == null) return null;
        return "OPEN".equals(currentOpeningStatus) ? "영업 중" : "영업 종료";
    }

    public LocalDateTime getNextClosingTimeAsLocalDateTime() {
        if (nextClosingTime == null || nextClosingTime.getDateTime() == null) return null;
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(nextClosingTime.getDateTime());
            return zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }

    public String[] getWeekdayDescriptions() {
        if (openingHours == null) return new String[0];
        return openingHours.getWeekdayDescriptions();
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

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NextClosingTime {

        @JsonProperty("dateTime")
        private String dateTime;

    }

}
