package com.timespot.backend.domain.station.api;

import static com.timespot.backend.common.response.SuccessCode.FAVORITE_CREATE_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.FAVORITE_DELETE_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.FAVORITE_GET_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.STATION_GET_SUCCESS;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.annotation.CustomPageResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.common.util.SortUtils;
import com.timespot.backend.domain.favorite.dto.FavoriteResponseDto.FavoriteListResponse;
import com.timespot.backend.domain.favorite.service.FavoriteService;
import com.timespot.backend.domain.station.dto.StationResponseDto.StationListResponse;
import com.timespot.backend.domain.station.dto.StationResponseDto.StationSearchResponse;
import com.timespot.backend.domain.station.service.StationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.timespot.backend.domain.station.api
 * FileName    : StationController
 * Author      : whitecity01
 * Date        : 26. 3. 26.
 * Description : 역 API 컨트롤러
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 26.    whitecity01        Initial creation
 * 26. 3. 26.    loadingKKamo21     API 로직 수정
 * 26. 3. 26.    loadingKKamo21     즐겨찾기 API 병합 (/api/v1/stations/favorites)
 */
@RestController
@RequestMapping("/api/v1/stations")
@RequiredArgsConstructor
public class StationController implements StationApiDocs {

    private static final double DEFAULT_RADIUS_METERS = 2000.0;  // 기본 검색 반경: 2km
    private static final double MAX_RADIUS_METERS     = 20000.0; // 최대 검색 반경: 20km

    private final StationService  stationService;
    private final FavoriteService favoriteService;

    @GetMapping
    @CustomPageResponse(
            numberOfElements = false,
            sort = false,
            empty = false,
            hasContent = false,
            first = false,
            last = false,
            hasPrevious = false
    )
    @Override
    public ResponseEntity<BaseResponse<StationSearchResponse>> getStations(
            @RequestParam("userLat") final double userLat,
            @RequestParam("userLon") final double userLon,
            @RequestParam(required = false, defaultValue = "2000") final double radius,
            @RequestParam(required = false, defaultValue = "") final String keyword,
            @RequestParam(required = false, defaultValue = "1") final int page,
            @RequestParam(required = false, defaultValue = "10") final int size,
            @RequestParam(required = false, defaultValue = "stationName,ASC") final String sort,
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        UUID userId = (userDetails != null) ? userDetails.getId() : null;

        List<StationListResponse> favoriteStations = (userId != null)
                                                     ? stationService.getFavoriteStationList(userId, keyword)
                                                     : List.of();

        List<StationListResponse> nearbyStations = stationService.getNearbyStationList(
                userLat, userLon, radius, keyword
        );

        Pageable                  pageable = SortUtils.createPageable(page, size, sort);
        Page<StationListResponse> stations = stationService.getStationList(keyword, pageable);

        StationSearchResponse responseData = new StationSearchResponse(
                favoriteStations,
                nearbyStations,
                stations
        );

        return ResponseEntity.ok(BaseResponse.success(STATION_GET_SUCCESS, responseData));
    }

    @PostMapping("/favorites/{stationId}")
    @Override
    public ResponseEntity<BaseResponse<Void>> createFavoriteStation(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @PathVariable final Long stationId
    ) {
        favoriteService.createFavoriteStation(userDetails.getId(), stationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(FAVORITE_CREATE_SUCCESS));
    }

    @DeleteMapping("/favorites/{stationId}")
    @Override
    public ResponseEntity<BaseResponse<Void>> deleteFavoriteStation(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @PathVariable final Long stationId
    ) {
        favoriteService.deleteFavoriteStation(userDetails.getId(), stationId);
        return ResponseEntity.ok(BaseResponse.success(FAVORITE_DELETE_SUCCESS));
    }

    @GetMapping("/favorites")
    @CustomPageResponse(
            numberOfElements = false,
            sort = false,
            empty = false,
            hasContent = false,
            first = false,
            last = false,
            hasPrevious = false
    )
    @Override
    public ResponseEntity<BaseResponse<Page<FavoriteListResponse>>> getFavoriteStationList(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @RequestParam(required = false, defaultValue = "") final String keyword,
            @RequestParam(required = false, defaultValue = "1") final int page,
            @RequestParam(required = false, defaultValue = "10") final int size,
            @RequestParam(required = false, defaultValue = "createdAt,DESC") final String sort
    ) {
        Pageable pageable = SortUtils.createPageable(page, size, sort);
        Page<FavoriteListResponse> responseData = favoriteService.getFavoriteStationList(
                userDetails.getId(), keyword, pageable
        );
        return ResponseEntity.ok(BaseResponse.success(FAVORITE_GET_SUCCESS, responseData));
    }

}
