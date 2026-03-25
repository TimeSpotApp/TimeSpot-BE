package com.timespot.backend.domain.history.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import com.timespot.backend.domain.history.model.VisitingHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.history.dto
 * FileName    : VisitingHistoryResponseDto
 * Author      : loadingKKamo21
 * Date        : 26. 3. 25.
 * Description : 방문 이력 도메인 응답 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 25.    loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(description = "방문 이력 도메인 응답 페이로드")
public abstract class VisitingHistoryResponseDto {

    @Getter
    @JsonInclude(NON_NULL)
    @Schema(description = "방문 이력 목록 응답 페이로드")
    public static class VisitingHistoryListResponse {

        @Schema(description = "방문 이력 ID", example = "1", accessMode = READ_ONLY)
        private final Long visitingHistoryId;

        @Schema(description = "역 ID", example = "1", accessMode = READ_ONLY)
        private final Long stationId;

        @Schema(description = "역 이름", example = "서울역", accessMode = READ_ONLY)
        private final String stationName;

        @Schema(description = "방문 장소 ID", example = "10", accessMode = READ_ONLY)
        private final Long placeId;

        @Schema(description = "방문 장소 이름", example = "스타벅스 서울역점", accessMode = READ_ONLY)
        private final String placeName;

        @Schema(description = "방문 장소 카테고리", example = "카페", accessMode = READ_ONLY)
        private final String placeCategory;

        @Schema(description = "여정 시작 시간 (ISO-8601 형식)", example = "2024-03-25T13:00:00", accessMode = READ_ONLY)
        private final LocalDateTime startTime;

        @Schema(description = "여정 종료 시간 (ISO-8601 형식, null 인 경우 진행 중)", example = "2024-03-25T14:30:00",
                accessMode = READ_ONLY)
        private final LocalDateTime endTime;

        @Schema(description = "열차 출발 시간 (ISO-8601 형식)", example = "2024-03-25T15:30:00", accessMode = READ_ONLY)
        private final LocalDateTime trainDepartureTime;

        @Schema(description = "총 소요 시간 (분)", example = "90", accessMode = READ_ONLY)
        private final Integer totalDurationMinutes;

        @Schema(description = "여정 진행 중 여부 (true: 진행 중, false: 완료)", example = "true", accessMode = READ_ONLY)
        private final Boolean isInProgress;

        @Schema(description = "여정 성공 여부 (true: 열차 출발 전 도착, false: 놓침)", example = "true", accessMode = READ_ONLY)
        private final Boolean isSuccess;

        @Schema(description = "여정 생성 일시 (ISO-8601 형식)", example = "2024-03-25T13:00:00", accessMode = READ_ONLY)
        private final LocalDateTime createdAt;

        @QueryProjection
        @JsonCreator
        public VisitingHistoryListResponse(final Long visitingHistoryId,
                                           final Long stationId,
                                           final String stationName,
                                           final Long placeId,
                                           final String placeName,
                                           final String placeCategory,
                                           final LocalDateTime startTime,
                                           final LocalDateTime endTime,
                                           final LocalDateTime trainDepartureTime,
                                           final Integer totalDurationMinutes,
                                           final boolean isInProgress,
                                           final boolean isSuccess,
                                           final LocalDateTime createdAt) {
            this.visitingHistoryId = visitingHistoryId;
            this.stationId = stationId;
            this.stationName = stationName;
            this.placeId = placeId;
            this.placeName = placeName;
            this.placeCategory = placeCategory;
            this.startTime = startTime;
            this.endTime = endTime;
            this.trainDepartureTime = trainDepartureTime;
            this.totalDurationMinutes = totalDurationMinutes;
            this.isInProgress = isInProgress;
            this.isSuccess = !isInProgress && isSuccess;
            this.createdAt = createdAt;
        }

    }

    @Getter
    @JsonInclude(NON_NULL)
    @Schema(description = "방문 이력 상세 응답 페이로드")
    public static class VisitingHistoryDetailResponse {

        @Schema(description = "방문 이력 ID", example = "1", accessMode = READ_ONLY)
        private final Long visitingHistoryId;

        @Schema(description = "역 ID", example = "1", accessMode = READ_ONLY)
        private final Long stationId;

        @Schema(description = "역 이름", example = "서울역", accessMode = READ_ONLY)
        private final String stationName;

        @Schema(description = "역 주소", example = "서울특별시 용산구 청암로 92", accessMode = READ_ONLY)
        private final String stationAddress;

        @Schema(description = "방문 장소 ID", example = "10", accessMode = READ_ONLY)
        private final Long placeId;

        @Schema(description = "방문 장소 이름", example = "스타벅스 서울역점", accessMode = READ_ONLY)
        private final String placeName;

        @Schema(description = "방문 장소 카테고리", example = "카페", accessMode = READ_ONLY)
        private final String placeCategory;

        @Schema(description = "방문 장소 주소", example = "서울특별시 용산구 청암로 90 1 층", accessMode = READ_ONLY)
        private final String placeAddress;

        @Schema(description = "여정 시작 시간 (ISO-8601 형식)", example = "2024-03-25T13:00:00", accessMode = READ_ONLY)
        private final LocalDateTime startTime;

        @Schema(description = "여정 종료 시간 (ISO-8601 형식, null 인 경우 진행 중)", example = "2024-03-25T14:30:00",
                accessMode = READ_ONLY)
        private final LocalDateTime endTime;

        @Schema(description = "열차 출발 시간 (ISO-8601 형식)", example = "2024-03-25T15:30:00", accessMode = READ_ONLY)
        private final LocalDateTime trainDepartureTime;

        @Schema(description = "총 소요 시간 (분)", example = "90", accessMode = READ_ONLY)
        private final Integer totalDurationMinutes;

        @Schema(description = "여정 진행 중 여부 (true: 진행 중, false: 완료)", example = "true", accessMode = READ_ONLY)
        private final Boolean isInProgress;

        @Schema(description = "여정 성공 여부 (true: 열차 출발 전 도착, false: 놓침)", example = "true", accessMode = READ_ONLY)
        private final Boolean isSuccess;

        @Schema(description = "여정 생성 일시 (ISO-8601 형식)", example = "2024-03-25T13:00:00", accessMode = READ_ONLY)
        private final LocalDateTime createdAt;

        @QueryProjection
        @JsonCreator
        public VisitingHistoryDetailResponse(final Long visitingHistoryId,
                                             final Long stationId,
                                             final String stationName,
                                             final String stationAddress,
                                             final Long placeId,
                                             final String placeName,
                                             final String placeCategory,
                                             final String placeAddress,
                                             final LocalDateTime startTime,
                                             final LocalDateTime endTime,
                                             final LocalDateTime trainDepartureTime,
                                             final Integer totalDurationMinutes,
                                             final boolean isInProgress,
                                             final boolean isSuccess,
                                             final LocalDateTime createdAt) {
            this.visitingHistoryId = visitingHistoryId;
            this.stationId = stationId;
            this.stationName = stationName;
            this.stationAddress = stationAddress;
            this.placeId = placeId;
            this.placeName = placeName;
            this.placeCategory = placeCategory;
            this.placeAddress = placeAddress;
            this.startTime = startTime;
            this.endTime = endTime;
            this.trainDepartureTime = trainDepartureTime;
            this.totalDurationMinutes = totalDurationMinutes;
            this.isInProgress = isInProgress;
            this.isSuccess = !isInProgress && isSuccess;
            this.createdAt = createdAt;
        }

        public static VisitingHistoryDetailResponse from(final VisitingHistory visitingHistory) {
            return new VisitingHistoryDetailResponse(
                    visitingHistory.getId(),
                    visitingHistory.getStation().getId(),
                    visitingHistory.getStation().getName(),
                    visitingHistory.getStation().getAddress(),
                    visitingHistory.getPlace().getId(),
                    visitingHistory.getPlace().getName(),
                    visitingHistory.getPlace().getCategory(),
                    visitingHistory.getPlace().getAddress(),
                    visitingHistory.getStartTime(),
                    visitingHistory.getEndTime(),
                    visitingHistory.getTrainDepartureTime(),
                    visitingHistory.getTotalDurationMinutes(),
                    visitingHistory.isInProgress(),
                    visitingHistory.isSuccess(),
                    visitingHistory.getCreatedAt()
            );
        }

    }

}
