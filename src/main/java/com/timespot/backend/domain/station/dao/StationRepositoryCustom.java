package com.timespot.backend.domain.station.dao;

import com.timespot.backend.domain.station.dto.StationResponseDto.StationListResponse;
import java.util.List;
import java.util.UUID;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * PackageName : com.timespot.backend.domain.station.dao
 * FileName    : StationRepositoryCustom
 * Author      : loadingKKamo21
 * Date        : 26. 3. 26.
 * Description : 역 리포지토리 커스텀 인터페이스 (QueryDSL)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 26.    loadingKKamo21       Initial creation
 */
public interface StationRepositoryCustom {

    List<StationListResponse> findFavoriteStationList(UUID userId, String keyword);

    List<StationListResponse> findNearbyStationList(Point point, double radius, String keyword);

    Page<StationListResponse> findStationList(String keyword, Pageable pageable);

}
