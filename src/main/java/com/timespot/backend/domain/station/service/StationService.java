package com.timespot.backend.domain.station.service;

import com.timespot.backend.domain.station.dto.StationResponseDto.StationList;
import com.timespot.backend.domain.station.dto.StationResponseDto.StationListResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
 * 26. 3. 26.     loadingKKamo21    getFavoriteStationList, getNearbyStationList, getStationList 추가
 */
public interface StationService {
    StationList getStationLists(UUID userId, double lat, double lng);

    List<StationListResponse> getFavoriteStationList(UUID userId, String keyword);

    List<StationListResponse> getNearbyStationList(double lat, double lng, double radius, String keyword);

    Page<StationListResponse> getStationList(String keyword, Pageable pageable);

    void clearStationCache();

}