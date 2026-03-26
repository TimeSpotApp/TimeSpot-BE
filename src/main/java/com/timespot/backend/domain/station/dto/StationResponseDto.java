package com.timespot.backend.domain.station.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * PackageName : com.timespot.backend.domain.station.dto
 * FileName    : StationResponseDto
 * Author      : whitecity01
 * Date        : 26. 3. 26.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 26.     whitecity01       Initial creation
 */
public class StationResponseDto {

    public interface StationProjection {
        Long getStationId();

        String getName();

        String getLineName();

        Integer getIsFavorite();
    }

    @Getter
    public static class StationDto {
        private Long         stationId;
        private String       name;
        private List<String> lines;
        private boolean      isFavorite;

        public StationDto(StationProjection projection) {
            this.stationId = projection.getStationId();
            this.name = projection.getName();

            this.isFavorite = projection.getIsFavorite() != null && projection.getIsFavorite() == 1;

            this.lines = projection.getLineName() != null && !projection.getLineName().isBlank()
                         ? Arrays.asList(projection.getLineName().split(","))
                         : Collections.emptyList();
        }
    }

    @Getter
    @AllArgsConstructor
    public static class StationList {
        private List<StationDto> favoriteStations;
        private List<StationDto> nearbyStations;
        private List<StationDto> allStations;
    }

    @Getter
    @RequiredArgsConstructor
    @JsonInclude(NON_NULL)
    @Schema(description = "역 전체 목록 응답 페이로드")
    public static class StationSearchResponse {
        private final List<StationListResponse> favoriteStations;
        private final List<StationListResponse> nearbyStations;
        private final Page<StationListResponse> stations;
    }

    @Getter
    @JsonInclude(NON_NULL)
    @Schema(description = "역 목록 응답 페이로드")
    public static class StationListResponse {

        @Schema(description = "역 ID", example = "1", accessMode = READ_ONLY)
        private final Long stationId;

        @Schema(description = "역 이름", example = "서울역", accessMode = READ_ONLY)
        private final String name;

        @Schema(description = "노선명", example = "[경부선, 호남선, 전라선]", accessMode = READ_ONLY)
        private final List<String> lines;

        @QueryProjection
        @JsonCreator
        public StationListResponse(final Long stationId, final String name, final String lines) {
            this.stationId = stationId;
            this.name = name;
            this.lines = (lines != null && !lines.isBlank())
                         ? Arrays.stream(lines.split(",")).map(String::trim).toList()
                         : Collections.emptyList();
        }

    }

}