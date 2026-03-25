package com.timespot.backend.domain.station.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        private Long stationId;
        private String name;
        private List<String> lines;
        private boolean isFavorite;

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
}