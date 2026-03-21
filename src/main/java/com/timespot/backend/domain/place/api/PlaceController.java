package com.timespot.backend.domain.place.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.SuccessCode;
import com.timespot.backend.common.response.annotation.CustomPageResponse;
import com.timespot.backend.domain.place.dto.PlaceResponseDto;
import com.timespot.backend.domain.place.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.timespot.backend.domain.place.api
 * FileName    : PlaceController
 * Author      : whitecity01
 * Date        : 26. 3. 19.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 19.     whitecity01       Initial creation
 * 26. 3. 20.     whitecity01       FIX getAvailablePlaces api url
 * 26. 3. 22.     whitecity01       ADD pagenation
 */
@RestController
@RequestMapping("/api/v1/place")
@RequiredArgsConstructor
public class PlaceController implements PlaceApiDocs {
    private final PlaceService placeService;

    // 호출 예시: GET /api/v1/place?userLat=37.559&userLon=126.977&stationId=1&remainingMinutes=40&page=0&size=10
    @Override
    @CustomPageResponse(totalPages = false, first = false, last = false)
    @GetMapping()
    public ResponseEntity<BaseResponse<Page<PlaceResponseDto.AvailablePlace>>> getAvailablePlaces(
            @RequestParam double userLat,
            @RequestParam double userLon,
            @RequestParam Long stationId,
            @RequestParam int remainingMinutes,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<PlaceResponseDto.AvailablePlace> places = placeService.getAvailablePlaces(
                userLat, userLon, stationId, remainingMinutes, pageable);

        return ResponseEntity.ok(BaseResponse.success(SuccessCode.PLACE_GET_AVAILABLE_PLACES_SUCCESS, places));
    }
}
