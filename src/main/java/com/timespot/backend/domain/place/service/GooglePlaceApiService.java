package com.timespot.backend.domain.place.service;

import com.timespot.backend.domain.place.dto.GooglePlaceDto;

/**
 * PackageName : com.timespot.backend.domain.place.service
 * FileName    : GooglePlaceApiService
 * Author      : whitecity01
 * Date        : 26. 3. 22.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 22.     whitecity01       ADD place detail
 */
public interface GooglePlaceApiService {
    GooglePlaceDto.ParsedResult getPlaceDetails(String googlePlaceId);
}
