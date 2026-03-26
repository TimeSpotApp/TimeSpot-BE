package com.timespot.backend.domain.station.service;

import com.timespot.backend.domain.station.dao.StationRepository;
import com.timespot.backend.domain.station.dto.StationResponseDto.StationDto;
import com.timespot.backend.domain.station.dto.StationResponseDto.StationList;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
 */
@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService {

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

    // UUID를 MySQL BINARY(16) Native Query 파라미터용 byte 배열로 변환
    private byte[] convertUuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}