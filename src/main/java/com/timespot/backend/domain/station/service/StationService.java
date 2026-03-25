package com.timespot.backend.domain.station.service;

import com.timespot.backend.domain.station.dto.StationResponseDto.StationList;

import java.util.UUID;

/**
 * PackageName : com.timespot.backend.domain.station.service
 * FileName    : StationService
 * Author      : whitecity01
 * Date        : 26. 3. 26.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 26.     whitecity01       Initial creation
 */
public interface StationService {
    StationList getStationLists(UUID userId, double lat, double lng);

    void toggleFavorite(UUID userId, Long stationId);
}