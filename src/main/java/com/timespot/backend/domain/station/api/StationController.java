package com.timespot.backend.domain.station.api;

import static com.timespot.backend.common.response.SuccessCode.STATION_GET_SUCCESS;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.annotation.CustomPageResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.common.util.SortUtils;
import com.timespot.backend.domain.station.dto.StationResponseDto.StationListResponse;
import com.timespot.backend.domain.station.dto.StationResponseDto.StationSearchResponse;
import com.timespot.backend.domain.station.service.StationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
 */
@RestController
@RequestMapping("/api/v1/stations")
@RequiredArgsConstructor
public class StationController implements StationApiDocs {

    private static final double DEFAULT_RADIUS_METERS = 2000.0;  // 기본 검색 반경: 2km
    private static final double MAX_RADIUS_METERS     = 10000.0; // 최대 검색 반경: 10km

    private final StationService stationService;

    /**
     * 역 목록 통합 조회 (즐겨찾기 + 근처 + 전체)
     *
     * @param lat         사용자 위도
     * @param lng         사용자 경도
     * @param radius      검색 반경 (미터 단위, 기본값: 2000)
     * @param keyword     검색어 (선택)
     * @param page        페이지 번호
     * @param size        페이지 크기
     * @param sort        정렬 기준
     * @param userDetails 스프링 시큐리티 인증 객체 (선택)
     * @return 역 목록 통합 응답 (StationSearchResponse)
     */
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
    public ResponseEntity<BaseResponse<StationSearchResponse>> getStations(
            @RequestParam("lat") final double lat,
            @RequestParam("lng") final double lng,
            @RequestParam(required = false, defaultValue = "2000") @Min(
                    value = 1, message = "검색 반경은 1m 이상이어야 합니다."
            ) @Max(
                    value = 10000, message = "검색 반경은 최대 10000m 입니다."
            ) final double radius,
            @RequestParam(required = false, defaultValue = "") final String keyword,
            @RequestParam(required = false, defaultValue = "1") @Min(1) final int page,
            @RequestParam(required = false, defaultValue = "10") @Min(10) final int size,
            @RequestParam(required = false, defaultValue = "stationName,ASC") @Pattern(
                    regexp = "^stationName,(ASC|DESC|asc|desc)(,\\s*stationName,(ASC|DESC|asc|desc))*$",
                    message = "정렬 형식이 올바르지 않습니다. (예: stationName,ASC)"
            ) final String sort,
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        UUID userId = (userDetails != null) ? userDetails.getId() : null;

        List<StationListResponse> favoriteStations = (userId != null)
                                                     ? stationService.getFavoriteStationList(userId, keyword)
                                                     : List.of();

        List<StationListResponse> nearbyStations = stationService.getNearbyStationList(
                lat, lng, radius, keyword
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

    /*
    @GetMapping()
    @Override
    public ResponseEntity<BaseResponse<StationList>> getStations(
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID userId = (userDetails != null) ? userDetails.getId() : null;

        StationList stationList = stationService.getStationLists(userId, lat, lng);

        return ResponseEntity.ok(BaseResponse.success(SuccessCode.STATION_GET_SUCCESS, stationList));
    }
    */

}
