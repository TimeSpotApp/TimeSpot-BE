package com.timespot.backend.domain.station.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.station.dto.StationResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * PackageName : com.timespot.backend.domain.station.api
 * FileName    : StationApiDocs
 * Author      : whitecity01
 * Date        : 26. 3. 26.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 26.     whitecity01       Initial creation
 */
@Tag(name = "Station API", description = "역 API")
public interface StationApiDocs {
    @Operation(summary = "역 조회", description = "사용자 즐겨찾기 역, 가까운 역, 모든 역 정보를 제공합니다.")
    @ApiResponse(responseCode = "200", description = "역 조회 완료")
    ResponseEntity<BaseResponse<StationResponseDto.StationList>> getStations(
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );
}
