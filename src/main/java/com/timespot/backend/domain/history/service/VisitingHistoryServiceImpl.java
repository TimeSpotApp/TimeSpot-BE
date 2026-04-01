package com.timespot.backend.domain.history.service;

import static com.timespot.backend.common.response.ErrorCode.HISTORY_NOT_FOUND;
import static com.timespot.backend.common.response.ErrorCode.STATION_NOT_FOUND;
import static com.timespot.backend.common.response.ErrorCode.USER_NOT_FOUND;
import static com.timespot.backend.domain.place.constant.PlaceConst.WALK_SPEED_PER_MINUTE;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.domain.favorite.dao.FavoriteRepository;
import com.timespot.backend.domain.history.dao.VisitingHistoryRepository;
import com.timespot.backend.domain.history.dto.VisitingHistoryRequestDto.JourneyEndRequest;
import com.timespot.backend.domain.history.dto.VisitingHistoryRequestDto.JourneyStartRequest;
import com.timespot.backend.domain.history.dto.VisitingHistoryResponseDto.VisitingHistoryDetailResponse;
import com.timespot.backend.domain.history.dto.VisitingHistoryResponseDto.VisitingHistoryListResponse;
import com.timespot.backend.domain.history.event.JourneyStartedEvent;
import com.timespot.backend.domain.history.model.VisitingHistory;
import com.timespot.backend.domain.history.notification.JourneyNotificationScheduler;
import com.timespot.backend.domain.station.dao.StationRepository;
import com.timespot.backend.domain.station.model.Station;
import com.timespot.backend.domain.user.dao.UserRepository;
import com.timespot.backend.domain.user.model.User;
import com.timespot.backend.infra.redis.model.PlaceCardCache;
import com.timespot.backend.infra.visitkorea.service.VisitKoreaPlaceService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PackageName : com.timespot.backend.domain.history.service
 * FileName    : VisitingHistoryServiceImpl
 * Author      : loadingKKamo21
 * Date        : 26. 3. 25.
 * Description : 방문 이력 서비스 구현체
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 25.    loadingKKamo21       Initial creation
 * 26. 3. 26.    loadingKKamo21       이미 종료된 이력 접근 차단 및 즐겨찾기 방문 통계 업데이트 추가
 * 26. 4. 1.     loadingKKamo21       WalkTimeFromPlace 계산 로직 추가, VisitKoreaPlaceService 의존성 추가
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class VisitingHistoryServiceImpl implements VisitingHistoryService {

    private final GeometryFactory geometryFactory = new GeometryFactory();

    private final VisitingHistoryRepository    visitingHistoryRepository;
    private final UserRepository               userRepository;
    private final StationRepository            stationRepository;
    private final FavoriteRepository           favoriteRepository;
    private final VisitKoreaPlaceService       visitKoreaPlaceService;
    private final ApplicationEventPublisher    eventPublisher;
    private final JourneyNotificationScheduler journeyNotificationScheduler;
    private final Clock                        clock;

    @Override
    @Transactional
    public VisitingHistoryDetailResponse createNewJourney(final UUID userId, final JourneyStartRequest dto) {
        User    user    = getUserById(userId);
        Station station = getStationById(dto.getStationId());

        PlaceCardCache placeCard = visitKoreaPlaceService.getPlaceCardWithFallback(dto.getPlaceId());

        Point placeLocation = geometryFactory.createPoint(new Coordinate(placeCard.getLongitude(),
                                                                         placeCard.getLatitude()));
        placeLocation.setSRID(4326);

        Long historyId = visitingHistoryRepository.save(
                VisitingHistory.of(
                        user,
                        station,
                        dto.getPlaceId(),           // placeContentId (VisitKorea contentId)
                        placeCard.getName(),        // placeName
                        placeCard.getCategory(),    // placeCategory
                        placeCard.getAddress(),     // placeAddress
                        placeLocation,              // placeLocation (POINT)
                        LocalDateTime.now(clock),   // startTime
                        dto.getTrainDepartureTime() // trainDepartureTime
                )
        ).getId();

        VisitingHistoryDetailResponse response = visitingHistoryRepository.findVisitingHistoryDetail(userId, historyId)
                                                                          .orElseThrow(() -> new GlobalException(
                                                                                  HISTORY_NOT_FOUND
                                                                          ));
        response.setStartLat(dto.getLat());
        response.setStartLng(dto.getLng());

        double distance = calculateDistance(
                placeCard.getLatitude(),
                placeCard.getLongitude(),
                station.getLatitude(),
                station.getLongitude()
        );
        int walkTimeFromPlace = (int) Math.ceil(distance / WALK_SPEED_PER_MINUTE);

        eventPublisher.publishEvent(new JourneyStartedEvent(
                userId,
                historyId,
                dto.getTrainDepartureTime(),
                walkTimeFromPlace
        ));

        return response;
    }

    @Override
    @Transactional
    public VisitingHistoryDetailResponse endJourney(final UUID userId,
                                                    final Long historyId,
                                                    final JourneyEndRequest dto) {
        VisitingHistory visitingHistory = visitingHistoryRepository.findByIdAndIsSuccessFalse(historyId)
                                                                   .orElseThrow(
                                                                           () -> new GlobalException(HISTORY_NOT_FOUND)
                                                                   );

        if (!visitingHistory.getUser().getId().equals(userId)) throw new GlobalException(HISTORY_NOT_FOUND);

        visitingHistory.validateEndable();

        if (dto.getIsCompleted()) {
            visitingHistory.endJourney(LocalDateTime.now(clock));

            if (visitingHistory.isSuccess()) {
                User    user            = visitingHistory.getUser();
                Station station         = visitingHistory.getStation();
                int     durationMinutes = visitingHistory.getTotalDurationMinutes();

                user.addVisitHistory(durationMinutes, true);

                updateFavoriteVisitCount(user, station, durationMinutes);
            }
        } else
            visitingHistory.abandonJourney();

        journeyNotificationScheduler.cancel(historyId);

        return visitingHistoryRepository.findVisitingHistoryDetail(userId, historyId)
                                        .orElseThrow(() -> new GlobalException(HISTORY_NOT_FOUND));
    }

    @Override
    public Page<VisitingHistoryListResponse> getVisitingHistoryList(final UUID userId,
                                                                    final String keyword,
                                                                    final Pageable pageable) {
        return visitingHistoryRepository.findVisitingHistoryList(userId, keyword, pageable);
    }

    @Override
    @Transactional
    public void deleteJourney(final UUID userId, final Long historyId) {
        VisitingHistory visitingHistory = visitingHistoryRepository.findById(historyId)
                                                                   .orElseThrow(
                                                                           () -> new GlobalException(HISTORY_NOT_FOUND)
                                                                   );

        if (!visitingHistory.getUser().getId().equals(userId)) throw new GlobalException(HISTORY_NOT_FOUND);

        User user = visitingHistory.getUser();
        if (visitingHistory.isSuccess() && visitingHistory.getTotalDurationMinutes() > 0)
            user.removeVisitHistory(visitingHistory.getTotalDurationMinutes(), true);
        else if (!visitingHistory.isInProgress()) user.removeVisitHistory(0, false);

        journeyNotificationScheduler.cancel(historyId);

        visitingHistoryRepository.delete(visitingHistory);
    }

    // ========================= 내부 메서드 =========================

    /**
     * 즐겨찾기 방문 횟수 및 누적 시간 업데이트
     * <p>
     * - 여정이 정상 완료된 경우에만 호출됨
     * - 해당 역의 즐겨찾기가 존재하는 경우 방문 횟수 + 여정 시간 증가
     * </p>
     *
     * @param user            사용자
     * @param station         역
     * @param durationMinutes 여정 시간 (분)
     */
    private void updateFavoriteVisitCount(final User user, final Station station, final int durationMinutes) {
        favoriteRepository.findByUserIdAndStationId(user.getId(), station.getId())
                          .ifPresent(favorite -> favorite.addVisitHistory(durationMinutes));
    }

    /**
     * 사용자 ID 로 사용자 조회
     *
     * @param userId 사용자 ID
     * @return 사용자 엔티티
     * @throws GlobalException 사용자를 찾을 수 없는 경우
     */
    private User getUserById(final UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new GlobalException(USER_NOT_FOUND));
    }

    /**
     * 역 ID 로 역 조회
     *
     * @param stationId 역 ID
     * @return 역 엔티티
     * @throws GlobalException 역을 찾을 수 없는 경우
     */
    private Station getStationById(final Long stationId) {
        return stationRepository.findById(stationId).orElseThrow(() -> new GlobalException(STATION_NOT_FOUND));
    }

    /**
     * 두 지점 간의 거리 계산 (Haversine 공식, 단위: 미터)
     */
    private double calculateDistance(final double lat1, final double lon1,
                                     final double lat2, final double lon2) {
        final int    EARTH_RADIUS = 6371000; // 지구 반경 (미터)
        final double dLat         = Math.toRadians(lat2 - lat1);
        final double dLon         = Math.toRadians(lon2 - lon1);

        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                         + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                           * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

}
