package com.timespot.backend.domain.history.dto;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.history.dto
 * FileName    : VisitingHistoryRequestDto
 * Author      : loadingKKamo21
 * Date        : 26. 3. 25.
 * Description : 방문 이력 요청 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 25.    loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(description = "방문 이력 도메인 요청 페이로드")
public abstract class VisitingHistoryRequestDto {

    /**
     * 여정 시작 요청 DTO
     * <p>
     * 사용자가 새로운 방문 이력을 시작할 때 사용하는 요청 페이로드입니다.
     * </p>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "여정 시작 요청 페이로드")
    public static class JourneyStartRequest {

        @NotNull(message = "역 ID 는 필수입니다.")
        @Schema(description = "[필수] 역 ID", example = "1", requiredMode = REQUIRED)
        private Long stationId;

        @NotNull(message = "장소 ID 는 필수입니다.")
        @Schema(description = "[필수] 방문 장소 ID", example = "10", requiredMode = REQUIRED)
        private Long placeId;

        @NotNull(message = "열차 출발 시간은 필수입니다.")
        @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @Schema(description = "[필수] 열차 출발 시간 (ISO-8601 형식)", example = "2024-03-25T15:30:00", requiredMode = REQUIRED)
        private LocalDateTime trainDepartureTime;

        @NotNull(message = "여정 시작 위치 위도는 필수입니다.")
        @Schema(description = "[필수] 여정 시작 위치 위도", example = "37.5665", requiredMode = REQUIRED)
        private Double lat;

        @NotNull(message = "여정 시작 위치 경도는 필수입니다.")
        @Schema(description = "[필수] 여정 시작 위치 경도", example = "126.9780", requiredMode = REQUIRED)
        private Double lng;

    }

    /**
     * 여정 종료 요청 DTO
     * <p>
     * 사용자가 진행 중인 여정을 종료할 때 사용하는 요청 페이로드입니다.
     * </p>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "여정 종료 요청 페이로드")
    public static class JourneyEndRequest {

        @NotNull(message = "여정 완료 여부는 필수입니다.")
        @Schema(description = "[필수] 여정 완료 여부 (true: 완료, false: 포기)", example = "true", requiredMode = REQUIRED)
        private Boolean isCompleted;

    }

}
