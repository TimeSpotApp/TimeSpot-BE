package com.timespot.backend.domain.place.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.domain.place.dto.PlaceResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * PackageName : com.timespot.backend.domain.place.api
 * FileName    : PlaceApiDocs
 * Author      : whitecity01
 * Date        : 26. 3. 19.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 19.     whitecity01       Initial creation
 * 26. 3. 22.     whitecity01       ADD pagenation
 */
@Tag(name = "Place API", description = "방문 가능 장소 조회 API")
public interface PlaceApiDocs {
    @Operation(summary = "방문 가능 장소 조회", description = "현재 사용자 위치에서 주어진 시간 내에 방문 후 복귀 가능한 장소를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "방문 가능 장소 조회 성공")
    ResponseEntity<BaseResponse<Page<PlaceResponseDto.AvailablePlace>>> getAvailablePlaces(
            @RequestParam double userLat,
            @RequestParam double userLon,
            @RequestParam Long stationId,
            @RequestParam int remainingMinutes,
            Pageable pageable
    );
}
