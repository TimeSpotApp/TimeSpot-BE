package com.timespot.backend.domain.favorite.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.favorite.dto
 * FileName    : FavoriteResponseDto
 * Author      : loadingKKamo21
 * Date        : 26. 3. 24.
 * Description : 즐겨찾기 도메인 응답 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 24.    loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(description = "즐겨찾기 도메인 응답 페이로드")
public abstract class FavoriteResponseDto {

    @Getter
    @JsonInclude(NON_NULL)
    @Schema(description = "즐겨찾기 역 목록 응답 페이로드")
    public static class FavoriteListResponse {

        @Schema(
                description = "즐겨찾기 ID",
                example = "1",
                accessMode = READ_ONLY
        )
        private final Long favoriteId;

        @Schema(
                description = "역 ID",
                example = "1",
                accessMode = READ_ONLY
        )
        private final Long stationId;

        @Schema(
                description = "역 이름",
                example = "서울역",
                accessMode = READ_ONLY
        )
        private final String stationName;

        @Schema(
                description = "방문 횟수 (즐겨찾기 역 방문 횟수)",
                example = "5",
                accessMode = READ_ONLY
        )
        private final Integer visitCount;

        @Schema(
                description = "총 방문 시간 (분)",
                example = "150",
                accessMode = READ_ONLY
        )
        private final Integer totalVisitMinutes;

        @Schema(
                description = "즐겨찾기 추가 일시 (ISO-8601 형식)",
                example = "2024-03-24T16:00:00",
                accessMode = READ_ONLY
        )
        private final LocalDateTime createdAt;

        @QueryProjection
        @JsonCreator
        public FavoriteListResponse(final Long favoriteId,
                                    final Long stationId,
                                    final String stationName,
                                    final Integer visitCount,
                                    final Integer totalVisitMinutes,
                                    final LocalDateTime createdAt) {
            this.favoriteId = favoriteId;
            this.stationId = stationId;
            this.stationName = stationName;
            this.visitCount = visitCount;
            this.totalVisitMinutes = totalVisitMinutes;
            this.createdAt = createdAt;
        }

    }

}
