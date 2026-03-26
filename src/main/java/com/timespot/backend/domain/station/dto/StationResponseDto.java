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
import org.locationtech.jts.geom.Point;
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

        @Schema(description = "즐겨찾기한 역 목록 (인증 시에만 populated)", accessMode = READ_ONLY)
        private final List<StationListResponse> favoriteStations;

        @Schema(description = "근처 역 목록 (거리 기준 오름차순, 최대 5 개)", accessMode = READ_ONLY)
        private final List<StationListResponse> nearbyStations;

        @Schema(description = "역 목록 (페이징 적용, @CustomPageResponse 에 의해 자동 변환)", accessMode = READ_ONLY)
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

        @Schema(description = "역 위도", example = "37.5665", accessMode = READ_ONLY)
        private final Double lat;

        @Schema(description = "역 경도", example = "126.9680", accessMode = READ_ONLY)
        private final Double lng;

        @Schema(description = "노선명", example = "[경부선, 호남선, 전라선]", accessMode = READ_ONLY)
        private final List<String> lines;

        @QueryProjection
        @JsonCreator
        public StationListResponse(final Long stationId, final String name, final Point location, final String lines) {
            this.stationId = stationId;
            this.name = name;
            this.lat = location.getY();
            this.lng = location.getX();
            this.lines = (lines != null && !lines.isBlank())
                         ? Arrays.stream(lines.split(",")).map(String::trim).toList()
                         : Collections.emptyList();
        }

    }

}
