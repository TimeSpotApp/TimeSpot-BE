package com.timespot.backend.domain.station.service;

import com.timespot.backend.domain.station.dao.StationRepository;
import com.timespot.backend.domain.station.dto.StationResponseDto.StationDto;
import com.timespot.backend.domain.station.dto.StationResponseDto.StationList;
import com.timespot.backend.domain.station.dto.StationResponseDto.StationListResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PackageName : com.timespot.backend.domain.station.service
 * FileName    : StationServiceImpl
 * Author      : whitecity01
 * Date        : 26. 3. 26.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 26.     whitecity01       Initial creation
 * 26. 3. 26.     loadingKKamo21    getFavoriteStationList, getNearbyStationList, getStationList 추가
 */
@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService {

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    private final StationRepository stationRepository;

    @Transactional(readOnly = true)
    @Override
    public StationList getStationLists(UUID userId, double lat, double lng) {
        byte[] userBytes = (userId != null) ? convertUuidToBytes(userId) : null;

        List<StationDto> nearbyStations = stationRepository
                .findNearbyStationsWithFavoriteStatus(userBytes, lat, lng)
                .stream().map(StationDto::new).collect(Collectors.toList());

        List<StationDto> allStations = stationRepository
                .findAllWithFavoriteStatus(userBytes)
                .stream().map(StationDto::new).collect(Collectors.toList());

        List<StationDto> favoriteStations = allStations.stream()
                                                       .filter(StationDto::isFavorite)
                                                       .collect(Collectors.toList());

        return new StationList(favoriteStations, nearbyStations, allStations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StationListResponse> getFavoriteStationList(final UUID userId, final String keyword) {
        return stationRepository.findFavoriteStationList(userId, keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StationListResponse> getNearbyStationList(final double userLat,
                                                          final double userLon,
                                                          final double radius,
                                                          final String keyword) {
        Point point = geometryFactory.createPoint(new Coordinate(userLon, userLat));
        return stationRepository.findNearbyStationList(point, radius, keyword);
    }

    @Override
    @Cacheable(
            value = "stations",
            key = "#pageable.pageNumber",
            condition = "(#keyword == null or #keyword.trim().empty)" +
                        " and #pageable.pageNumber < 10" +
                        " and #root.target.isDefaultSort(#pageable)"
    )
    @Transactional(readOnly = true)
    public Page<StationListResponse> getStationList(final String keyword, final Pageable pageable) {
        return stationRepository.findStationList(keyword, pageable);
    }

    @Override
    @CacheEvict(value = "stations", allEntries = true)
    public void clearStationCache() {}

    public boolean isDefaultSort(final Pageable pageable) {
        Sort sort = pageable.getSort();
        return sort.isUnsorted() && sort.stream().findAny().isEmpty();
    }

    // UUID를 MySQL BINARY(16) Native Query 파라미터용 byte 배열로 변환
    private byte[] convertUuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
