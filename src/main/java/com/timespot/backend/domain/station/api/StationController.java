package com.timespot.backend.domain.station.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.SuccessCode;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.station.dto.StationResponseDto.StationList;
import com.timespot.backend.domain.station.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * PackageName : com.timespot.backend.domain.station.api
 * FileName    : StationController
 * Author      : whitecity01
 * Date        : 26. 3. 26.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 26.     whitecity01       Initial creation
 */
@RestController
@RequestMapping("/api/v1/stations")
@RequiredArgsConstructor
public class StationController implements StationApiDocs{

    private final StationService stationService;

    /**
     * 역 목록 전체 조회 (즐겨찾기, 가까운 순 포함)
     * @param lat 사용자 위도
     * @param lng 사용자 경도
     * @param userDetails 스프링 시큐리티 인증 객체
     */
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

    /**
     * 역 즐겨찾기 추가/삭제
     * @param stationId 역ID
     * @param userDetails 스프링 시큐리티 인증 객체
     */
    @PostMapping("/{stationId}/favorites")
    @Override
    public ResponseEntity<BaseResponse<Void>> toggleFavorite(
            @PathVariable("stationId") Long stationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID userId = userDetails.getId();

        stationService.toggleFavorite(userId, stationId);
        return ResponseEntity.ok(BaseResponse.success(SuccessCode.STATION_FAVORITE_SUCCESS, null));
    }
}
