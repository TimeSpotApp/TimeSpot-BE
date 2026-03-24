package com.timespot.backend.domain.place.service;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.response.ErrorCode;
import com.timespot.backend.domain.place.constant.PlaceConst;
import com.timespot.backend.domain.place.dao.PlaceRepository;
import com.timespot.backend.domain.place.dto.GooglePlaceDto;
import com.timespot.backend.domain.place.dto.PlaceResponseDto;
import com.timespot.backend.domain.place.model.Station;
import com.timespot.backend.domain.place.dao.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * PackageName : com.timespot.backend.domain.place.service
 * FileName    : PlaceServiceImpl
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
@Service
@RequiredArgsConstructor
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository       placeRepository;
    private final StationRepository     stationRepository;
    private final GooglePlaceApiService googlePlaceApiService;

    /**
     * 유저 위치, 역ID, 남은 시간을 입력받아
     * (유저 위치 -> 장소 + 장소 -> 역) 거리가 남은 시간 내에 방문 가능한 장소 리스트 반환
     *
     * @param userLat          사용자 위도 정보
     * @param userLon          사용자 경도 정보
     * @param stationId        출발 역 ID
     * @param remainingMinutes 출발까지 남은 시간
     * @return 방문 가능한 장소 엔티티
     */
    @Override
    public Page<PlaceResponseDto.AvailablePlace> getAvailablePlaces(double userLat,
                                                                    double userLon,
                                                                    Long stationId,
                                                                    int remainingMinutes,
                                                                    Pageable pageable) {
        // 역 정보 조회 (역의 위경도 조회)
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new GlobalException(ErrorCode.STATION_NOT_FOUND));

        if(!station.getIsActive()) {
            throw new GlobalException(ErrorCode.STATION_NOT_ACTIVE);
        }

        if (remainingMinutes <= PlaceConst.TOTAL_BUFFER_TIME) {
            // 시간이 부족해서 어디도 갈 수 없는 상황
            throw new GlobalException(ErrorCode.PLACE_INSUFFICIENT_REMAINING_TIME);
        }

        // 남은 시간을 '이동 가능 거리(m)'로 환산
        int walkableDistance = (remainingMinutes- PlaceConst.TOTAL_BUFFER_TIME) * PlaceConst.WALK_SPEED_PER_MINUTE;

        List<PlaceResponseDto.AvailablePlace> places = placeRepository.findAvailablePlacesOnRoute(
                stationId, userLat, userLon, station.getLatitude(), station.getLongitude(), walkableDistance, PlaceConst.WALK_SPEED_PER_MINUTE, pageable
        );

        int totalCount = places.isEmpty() ? 0 : places.get(0).getTotalCount();

        return new PageImpl<>(places, pageable, totalCount);
    }

    /**
     * 장소 상세 정보 요청 -> 정제 -> 반환
     *
     * @param googleId   장소 구글id
     * @param stationId  역 id
     * @return 장소 세부 정보 엔티티
     */
    @Override
    public PlaceResponseDto.PlaceDetail getPlaceDetail(String googleId, Long stationId) {

        PlaceResponseDto.PlaceDetailInDB dbResult = placeRepository.findPlaceDetail(
                googleId, stationId, PlaceConst.WALK_SPEED_PER_MINUTE)
                .orElseThrow(() -> new GlobalException(ErrorCode.PLACE_NOT_FOUND));

        // 구글 API 호출
        GooglePlaceDto.ParsedResult googleApiResult = googlePlaceApiService.getPlaceDetails(googleId);

        return PlaceResponseDto.PlaceDetail.builder()
                .name(dbResult.getName())
                .category(dbResult.getCategory())
                .address(dbResult.getAddress())
                .distanceToStation(dbResult.getDistanceToStation())
                .timeToStation(dbResult.getTimeToStation())
                .imageUrl(googleApiResult.getImageUrl())
                .weekday(googleApiResult.getWeekdayHours())
                .weekend(googleApiResult.getWeekendHours())
                .phoneNumber(googleApiResult.getPhoneNumber())
                .build();
    }
}
