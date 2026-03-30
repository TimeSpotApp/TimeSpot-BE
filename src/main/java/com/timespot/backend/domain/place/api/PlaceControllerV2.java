package com.timespot.backend.domain.place.api;

import static com.timespot.backend.common.response.SuccessCode.PLACE_GET_AVAILABLE_PLACES_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.PLACE_GET_DETAIL_SUCCESS;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.annotation.CustomPageResponse;
import com.timespot.backend.common.util.SortUtils;
import com.timespot.backend.domain.place.dto.PlaceResponseDtoV2.AvailablePlace;
import com.timespot.backend.domain.place.dto.PlaceResponseDtoV2.PlaceDetail;
import com.timespot.backend.domain.place.service.PlaceServiceV2;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.timespot.backend.domain.place.api
 * FileName    : PlaceControllerV2
 * Author      : loadingKKamo21
 * Date        : 26. 3. 29.
 * Description : Place API V2 컨트롤러
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 29.    loadingKKamo21       Initial creation
 */
@RestController
@RequestMapping("/api/v2/places")
@RequiredArgsConstructor
public class PlaceControllerV2 implements PlaceApiDocsV2 {

    private final PlaceServiceV2 placeServiceV2;

    @GetMapping
    @CustomPageResponse(
            numberOfElements = false,
            empty = false,
            hasContent = false,
            first = false,
            last = false,
            hasPrevious = false
    )
    @Override
    public ResponseEntity<BaseResponse<Page<AvailablePlace>>> findAvailablePlaces(
            @RequestParam final Long stationId,
            @RequestParam final double userLat,
            @RequestParam final double userLon,
            @RequestParam(required = false) final Double mapLat,
            @RequestParam(required = false) final Double mapLon,
            @RequestParam final int remainingMinutes,
            @RequestParam(required = false, defaultValue = "") final String keyword,
            @RequestParam(required = false, defaultValue = "전체") final String category,
            @RequestParam(required = false, defaultValue = "1") final int page,
            @RequestParam(required = false, defaultValue = "50") final int size,
            @RequestParam(required = false, defaultValue = "distanceFromStation,ASC") final String sort
    ) {
        Pageable pageable = SortUtils.createPageable(page - 1, size, sort);

        Page<AvailablePlace> responseData = placeServiceV2.findAvailablePlaces(
                stationId,
                userLat,
                userLon,
                mapLat,
                mapLon,
                remainingMinutes,
                keyword,
                category,
                pageable
        );

        return ResponseEntity.ok(BaseResponse.success(PLACE_GET_AVAILABLE_PLACES_SUCCESS, responseData));
    }

    @GetMapping("/{placeId}")
    @Override
    public ResponseEntity<BaseResponse<PlaceDetail>> getPlaceDetail(
            @PathVariable final String placeId,
            @RequestParam final Long stationId,
            @RequestParam final double userLat,
            @RequestParam final double userLon,
            @RequestParam final int remainingMinutes
    ) {
        PlaceDetail responseData = placeServiceV2.getPlaceDetail(
                placeId, stationId, userLat, userLon, remainingMinutes
        );
        return ResponseEntity.ok(BaseResponse.success(PLACE_GET_DETAIL_SUCCESS, responseData));
    }

}
