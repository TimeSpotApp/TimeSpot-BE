package com.timespot.backend.domain.place.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.domain.place.constant.PlaceSortType;
import com.timespot.backend.domain.place.dto.PlaceResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
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
 * 26. 3. 27.     whitecity01       MODIFY getPlaceDetail response
 */
@Tag(name = "Place API", description = "방문 가능 장소 조회 API")
public interface PlaceApiDocs {
    @Operation(summary = "방문 가능 장소 조회", description = "현재 사용자 위치에서 주어진 시간 내에 방문 후 복귀 가능한 장소를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "방문 가능 장소 조회 성공")
    ResponseEntity<BaseResponse<List<PlaceResponseDto.SimpleAvailablePlace>>> getAvailablePlaces(
            @RequestParam double userLat,
            @RequestParam double userLon,
            @RequestParam double mapLat,
            @RequestParam double mapLon,
            @RequestParam Long stationId,
            @RequestParam int remainingMinutes
    );

    @Operation(summary = "장소 상세 정보 조회", description = "해당 장소의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "장소 상세 정보 조회 성공")
    ResponseEntity<BaseResponse<PlaceResponseDto.PlaceDetail>> getPlaceDetail(
            @RequestParam String googleId,
            @RequestParam Long stationId,
            @RequestParam double userLat,
            @RequestParam double userLon,
            @RequestParam int remainingMinutes
    );

    @Operation(summary = "장소 검색", description = "검색어와 필터에 맞는 장소를 검색합니다.")
    @ApiResponse(responseCode = "200", description = "장소 검색 성공")
    ResponseEntity<BaseResponse<Slice<PlaceResponseDto.SearchPlace>>> searchPlaces(
            @RequestParam double userLat,
            @RequestParam double userLon,
            @RequestParam Long stationId,
            @RequestParam int remainingMinutes,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "STATION_NEAREST") PlaceSortType sortBy,
            @RequestParam(required = false) Double markerLat,
            @RequestParam(required = false) Double markerLon,
            @PageableDefault(size = 10) Pageable pageable
    );
}
