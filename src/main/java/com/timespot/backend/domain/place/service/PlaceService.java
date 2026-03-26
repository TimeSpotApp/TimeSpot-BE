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
 * 26. 3. 22.     whitecity01       ADD pagenation
 * 26. 3. 22.     whitecity01       ADD place detail
 * 26. 3. 26.     whitecity01       MODIFY findAvailablePlacesOnRoute logic
 */
public interface PlaceService {
    List<PlaceResponseDto.AvailablePlace> getAvailablePlaces(double userLat,
                                                             double userLon,
                                                             double mapLat,
                                                             double mapLon,
                                                             Long stationId,
                                                             int remainingMinutes);

    PlaceResponseDto.PlaceDetail getPlaceDetail(String googleId, Long stationId);
}
