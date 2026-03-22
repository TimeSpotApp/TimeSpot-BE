package com.timespot.backend.domain.place.service;

import com.timespot.backend.domain.place.dto.PlaceResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


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
 */
public interface PlaceService {
    Page<PlaceResponseDto.AvailablePlace> getAvailablePlaces(double userLat,
                                                             double userLon,
                                                             Long stationId,
                                                             int remainingMinutes,
                                                             Pageable pageable);

    PlaceResponseDto.PlaceDetail getPlaceDetail(String googleId, Long stationId);
}
