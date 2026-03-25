package com.timespot.backend.domain.favorite.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static lombok.AccessLevel.PRIVATE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.favorite.dto
 * FileName    : FavoriteRequestDto
 * Author      : loadingKKamo21
 * Date        : 26. 3. 24.
 * Description : 즐겨찾기 요청 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 24.    loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(description = "즐겨찾기 도메인 요청 페이로드")
public abstract class FavoriteRequestDto {

    /**
     * 즐겨찾기 역 생성 요청 DTO
     * <p>
     * 사용자가 특정 역을 즐겨찾기에 추가할 때 사용하는 요청 페이로드입니다.
     * </p>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "즐겨찾기 역 생성 요청 페이로드")
    public static class FavoriteStationCreateRequest {

        @NotNull(message = "역 ID 는 필수입니다.")
        @Schema(description = "[필수] 역 ID", example = "1", requiredMode = REQUIRED)
        private Long stationId;

    }

}
