package com.timespot.backend.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.place.dto
 * FileName    : PlaceResponseDto
 * Author      : whitecity01
 * Date        : 26. 3. 19.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 19.     whitecity01       Initial creation
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "장소 도메인 응답 페이로드")
public abstract class PlaceResponseDto {

    @Schema(description = "방문 가능한 장소 응답 페이로드")
    public interface AvailablePlace {

        @Schema(description = "장소 이름", example = "스타벅스 서울역점")
        String getName();

        @Schema(description = "구글 플레이스 ID", example = "ChIJudLQD1mjfDUR3VmcnRfX3Yg")
        String getGooglePlaceId();

        @Schema(description = "장소 카테고리", example = "카페")
        String getCategory();

        @Schema(description = "도로명 주소", example = "서울특별시 용산구 동자동 11-111")
        String getAddress();

        @Schema(description = "위도", example = "30.1111")
        Double getLat();

        @Schema(description = "경도", example = "120.1111")
        Double getLon();
    }
}