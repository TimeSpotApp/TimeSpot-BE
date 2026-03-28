package com.timespot.backend.domain.place.service;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.response.ErrorCode;
import com.timespot.backend.domain.place.constant.PlaceConst;
import com.timespot.backend.domain.place.constant.PlaceSortType;
import com.timespot.backend.domain.place.dao.PlaceRepository;
import com.timespot.backend.domain.place.dto.GooglePlaceDto;
import com.timespot.backend.domain.place.dto.PlaceResponseDto;
import com.timespot.backend.domain.station.dao.StationRepository;
import com.timespot.backend.domain.station.model.Station;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
 * 26. 3. 26.     whitecity01       DIVIDE station domain
 * 26. 3. 26.     whitecity01       MODIFY findAvailablePlacesOnRoute logic
 * 26. 3. 27.     whitecity01       ADD place search
 * 26. 3. 27.     whitecity01       MODIFY getPlaceDetail response
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
    public List<PlaceResponseDto.SimpleAvailablePlace> getAvailablePlaces(double userLat, double userLon, double mapLat, double mapLon, Long stationId, int remainingMinutes) {

        Station station = getValidatedStation(stationId);
        int walkableDistance = calculateWalkableDistance(remainingMinutes);

        return placeRepository.findAvailablePlacesOnRoute(
                stationId, userLat, userLon, station.getLatitude(), station.getLongitude(), mapLat, mapLon, walkableDistance, PlaceConst.WALK_SPEED_PER_MINUTE
        );
    }

    /**
     * 장소 상세 정보 제공
     *
     * @param placeId          place ID
     * @param stationId        출발 역 ID
     * @return 장소 상세 정보 엔티티
     */
    @Override
    public PlaceResponseDto.PlaceDetail getPlaceDetail(Long placeId, Long stationId, double userLat, double userLon, int remainingMinutes) {

        Station station = getValidatedStation(stationId);
        int walkableDistance = calculateWalkableDistance(remainingMinutes);

        PlaceResponseDto.PlaceDetailInDB dbResult = placeRepository.findPlaceDetail(
                        placeId, stationId, userLat, userLon, walkableDistance, PlaceConst.WALK_SPEED_PER_MINUTE)
                .orElseThrow(() -> new GlobalException(ErrorCode.PLACE_NOT_FOUND));

        if (dbResult.getStayableMinutes() < 0) {
            throw new GlobalException(ErrorCode.PLACE_INSUFFICIENT_REMAINING_TIME); // 체류 불가능
        }

        GooglePlaceDto.ParsedResult googleApiResult = googlePlaceApiService.getPlaceDetails(dbResult.getGooglePlaceId());

        LocalDateTime leaveTimeObj = LocalDateTime.now().plusMinutes(remainingMinutes - dbResult.getTimeToStation());
        String formattedLeaveTime = leaveTimeObj.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return PlaceResponseDto.PlaceDetail.builder()
                .name(dbResult.getName())
                .category(dbResult.getCategory())
                .address(dbResult.getAddress())
                .distanceToStation(dbResult.getDistanceToStation())
                .timeToStation(dbResult.getTimeToStation())
                .stayableMinutes(dbResult.getStayableMinutes() + PlaceConst.MINIMUM_STAY_TIME)
                .stationLat(station.getLatitude())
                .stationLon(station.getLongitude())
                .leaveTime(formattedLeaveTime)
                .imageUrl(googleApiResult.getImageUrls())
                .weekday(googleApiResult.getWeekdayHours())
                .weekend(googleApiResult.getWeekendHours())
                .phoneNumber(googleApiResult.getPhoneNumber())
                .build();
    }

    @Override
    public Slice<PlaceResponseDto.SearchPlace> searchPlaces(double userLat, double userLon, Long stationId, int remainingMinutes, String keyword, String category, PlaceSortType sortBy, Double mapLat, Double mapLon, Pageable pageable) {

        Station station = getValidatedStation(stationId);
        int walkableDistance = calculateWalkableDistance(remainingMinutes);

        // 마커 기준 정렬인데 마커 좌표가 없는 경우 예외 처리
        if (sortBy == PlaceSortType.MAP_NEAREST && (mapLat == null || mapLon == null)) {
            throw new GlobalException(ErrorCode.PLACE_INVALID_MAP);
        }

        // 쿼리 오류 방지를 위한 안전한 기본값 할당 (정렬 조건이 MARKER_NEAREST가 아닐 때 null 방지)
        double safeMapLat = mapLat != null ? mapLat : 0.0;
        double safeMapLon = mapLon != null ? mapLon : 0.0;

        String filterCategory = (category == null || category.trim().isEmpty() || "전체".equals(category)) ? null : category;
        String filterKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword;

        Pageable unpaged = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        Slice<PlaceResponseDto.AvailablePlace> dbPlaces = placeRepository.searchAvailablePlaces(
                stationId, userLat, userLon, station.getLatitude(), station.getLongitude(),
                safeMapLat, safeMapLon,
                walkableDistance, PlaceConst.WALK_SPEED_PER_MINUTE,
                filterKeyword,
                filterCategory,
                sortBy.name(),
                unpaged
        );

        // TODO : Google API 연동 및 캐싱 로직 추가
        String hardcodedClosingTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 22:00:00";

        return dbPlaces.map(place -> PlaceResponseDto.SearchPlace.builder()
                .name(place.getName())
                .placeId(place.getPlaceId())
                .category(place.getCategory())
                .address(place.getAddress())
                .lat(place.getLat())
                .lon(place.getLon())
                .stayableMinutes(place.getStayableMinutes() + PlaceConst.MINIMUM_STAY_TIME)
                .isOpen(true)                             // TODO : Google API 연동 및 캐싱 로직 추가
                .closingTime(hardcodedClosingTime)        // TODO : Google API 연동 및 캐싱 로직 추가
                .build()
        );
    }

    /**
     * 역 정보 조회 및 활성화 상태 검증
     */
    private Station getValidatedStation(Long stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new GlobalException(ErrorCode.STATION_NOT_FOUND));

        if (!station.getIsActive()) {
            throw new GlobalException(ErrorCode.STATION_NOT_ACTIVE);
        }
        return station;
    }

    /**
     * 남은 시간 검증 및 도보 가능 거리 계산
     */
    private int calculateWalkableDistance(int remainingMinutes) {
        if (remainingMinutes <= PlaceConst.TOTAL_BUFFER_TIME) {
            throw new GlobalException(ErrorCode.PLACE_INSUFFICIENT_REMAINING_TIME);
        }
        return (remainingMinutes - PlaceConst.TOTAL_BUFFER_TIME) * PlaceConst.WALK_SPEED_PER_MINUTE;
    }
}
