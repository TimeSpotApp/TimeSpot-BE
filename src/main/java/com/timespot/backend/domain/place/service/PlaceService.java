package com.timespot.backend.domain.place.service;

import com.timespot.backend.domain.place.dto.PlaceResponseDto;

import java.util.List;

/**
 * PackageName : com.timespot.backend.domain.place.service
 * FileName    : PlaceService
 * Author      : whitecity01
 * Date        : 26. 3. 19.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 19.     whitecity01       Initial creation
 */
public interface PlaceService {
    List<PlaceResponseDto.AvailablePlace> getAvailablePlaces(double userLat,
                                                             double userLon,
                                                             Long stationId,
                                                             int remainingMinutes);
}
