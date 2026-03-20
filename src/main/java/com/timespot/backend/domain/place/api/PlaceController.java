package com.timespot.backend.domain.place.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.SuccessCode;
import com.timespot.backend.domain.place.dto.PlaceResponseDto;
import com.timespot.backend.domain.place.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
 */
@RestController
@RequestMapping("/api/v1/place")
@RequiredArgsConstructor
public class PlaceController implements PlaceApiDocs {
    private final PlaceService placeService;

    // 호출 예시: GET /api/v1/place?userLat=37.559&userLon=126.977&stationId=1&remainingMinutes=40
    @Override
    @GetMapping()
    public ResponseEntity<BaseResponse<List<PlaceResponseDto.AvailablePlace>>> getAvailablePlaces(
            @RequestParam double userLat,
            @RequestParam double userLon,
            @RequestParam Long stationId,
            @RequestParam int remainingMinutes) {

        List<PlaceResponseDto.AvailablePlace> places = placeService.getAvailablePlaces(
                userLat, userLon, stationId, remainingMinutes);

        return ResponseEntity.ok(BaseResponse.success(SuccessCode.PLACE_GET_AVAILABLE_PLACES_SUCCESS, places));
    }
}
