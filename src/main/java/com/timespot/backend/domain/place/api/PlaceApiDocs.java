package com.timespot.backend.domain.place.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.domain.place.dto.PlaceResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


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
 * 26. 3. 22.     whitecity01       ADD place details
 * 26. 3. 26.     whitecity01       MODIFY findAvailablePlacesOnRoute logic
 */
@Tag(name = "Place API", description = "방문 가능 장소 조회 API")
public interface PlaceApiDocs {
    @Operation(summary = "방문 가능 장소 조회", description = "현재 사용자 위치에서 주어진 시간 내에 방문 후 복귀 가능한 장소를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "방문 가능 장소 조회 성공")
    ResponseEntity<BaseResponse<List<PlaceResponseDto.AvailablePlace>>> getAvailablePlaces(
            @RequestParam double userLat,
            @RequestParam double userLon,
            @RequestParam double mapLat, // 추가: 화면 중심 위도
            @RequestParam double mapLon, // 추가: 화면 중심 경도
            @RequestParam Long stationId,
            @RequestParam int remainingMinutes
    );

    @Operation(summary = "장소 상세 정보 조회", description = "해당 장소의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "장소 상세 정보 조회 성공")
    ResponseEntity<BaseResponse<PlaceResponseDto.PlaceDetail>> getPlaceDetail(
            @RequestParam String googleId,
            @RequestParam Long stationId
    );

}
