package com.timespot.backend.domain.place.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.SuccessCode;
import com.timespot.backend.common.response.annotation.CustomPageResponse;
import com.timespot.backend.domain.place.dto.PlaceResponseDto;
import com.timespot.backend.domain.place.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
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
 * 26. 3. 20.     whitecity01       FIX getAvailablePlaces api url
 * 26. 3. 22.     whitecity01       ADD pagenation
 * 26. 3. 22.     whitecity01       ADD place details
 * 26. 3. 26.     whitecity01       MODIFY findAvailablePlacesOnRoute logic
 * 26. 3. 27.     whitecity01       ADD place search
 */
@RestController
@RequestMapping("/api/v1/place")
@RequiredArgsConstructor
public class PlaceController implements PlaceApiDocs {
    private final PlaceService placeService;

    @Override
    @GetMapping()
    public ResponseEntity<BaseResponse<List<PlaceResponseDto.AvailablePlace>>> getAvailablePlaces(
            @RequestParam double userLat,
            @RequestParam double userLon,
            @RequestParam double mapLat,
            @RequestParam double mapLon,
            @RequestParam Long stationId,
            @RequestParam int remainingMinutes) {

        List<PlaceResponseDto.AvailablePlace> places = placeService.getAvailablePlaces(
                userLat, userLon, mapLat, mapLon, stationId, remainingMinutes);

        return ResponseEntity.ok(BaseResponse.success(SuccessCode.PLACE_GET_AVAILABLE_PLACES_SUCCESS, places));
    }

    @Override
    @GetMapping("/detail")
    public ResponseEntity<BaseResponse<PlaceResponseDto.PlaceDetail>> getPlaceDetail(
            @RequestParam String googleId,
            @RequestParam Long stationId) {

        PlaceResponseDto.PlaceDetail placeDetail = placeService.getPlaceDetail(googleId, stationId);

        return ResponseEntity.ok(BaseResponse.success(SuccessCode.PLACE_GET_DETAIL_SUCCESS, placeDetail));
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<BaseResponse<Slice<PlaceResponseDto.AvailablePlace>>> searchPlaces(
            @RequestParam double userLat,
            @RequestParam double userLon,
            @RequestParam Long stationId,
            @RequestParam int remainingMinutes,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "STATION_NEAREST") String sortBy,
            @PageableDefault(size = 10) Pageable pageable) {

        Slice<PlaceResponseDto.AvailablePlace> places = placeService.searchPlaces(
                userLat, userLon, stationId, remainingMinutes, keyword, category, sortBy, pageable);

        return ResponseEntity.ok(BaseResponse.success(SuccessCode.PLACE_GET_AVAILABLE_PLACES_SUCCESS, places));
    }
}
