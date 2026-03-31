package com.timespot.backend.domain.history.service;

import static com.timespot.backend.common.response.ErrorCode.STATION_NOT_FOUND;
import static com.timespot.backend.common.response.ErrorCode.USER_NOT_FOUND;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.domain.history.api.VisitingHistoryTestApiDocs.TestNotificationResponse;
import com.timespot.backend.domain.history.dao.VisitingHistoryRepository;
import com.timespot.backend.domain.history.event.JourneyStartedEvent;
import com.timespot.backend.domain.history.model.VisitingHistory;
import com.timespot.backend.domain.place.dao.PlaceRepository;
import com.timespot.backend.domain.place.model.Place;
import com.timespot.backend.domain.station.dao.StationRepository;
import com.timespot.backend.domain.station.model.Station;
import com.timespot.backend.domain.user.dao.UserRepository;
import com.timespot.backend.domain.user.model.NotificationTiming;
import com.timespot.backend.domain.user.model.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PackageName : com.timespot.backend.domain.history.service
 * FileName    : VisitingHistoryTestService
 * Author      : loadingKKamo21
 * Date        : 26. 3. 31.
 * Description : 방문 이력 알림 테스트 서비스 (기존 비즈니스 로직 수정 없음)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 31.    loadingKKamo21       Initial creation
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VisitingHistoryTestService {

    private final UserRepository            userRepository;
    private final StationRepository         stationRepository;
    private final PlaceRepository           placeRepository;
    private final VisitingHistoryRepository visitingHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 테스트용 여정을 생성하고 알림을 예약합니다.
     * <p>
     * 기존 비즈니스 로직을 전혀 수정하지 않고, 기존 이벤트 발행 로직을 활용합니다.
     * </p>
     *
     * @param userId           사용자 ID
     * @param remainingMinutes 열차 출발까지 남은 시간 (분)
     * @return 테스트 결과 응답
     */
    @Transactional
    public TestNotificationResponse testJourneyNotification(final UUID userId, final int remainingMinutes) {
        log.info("[NOTIFICATION TEST] 시작: userId={}, remainingMinutes={}", userId, remainingMinutes);

        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new GlobalException(USER_NOT_FOUND));

        Set<NotificationTiming> notificationTimings = user.getNotificationTimings();

        log.info("[NOTIFICATION TEST] 사용자 알림 설정: userId={}, timings={}", userId, notificationTimings);

        List<Station> allStations = stationRepository.findAll();
        if (allStations.isEmpty()) throw new GlobalException(STATION_NOT_FOUND);

        Station randomStation = allStations.get((int) (Math.random() * allStations.size()));
        log.info("[NOTIFICATION TEST] 선택된 역: stationId={}, name={}", randomStation.getId(), randomStation.getName());

        List<Place> allPlaces = placeRepository.findAll();
        if (allPlaces.isEmpty()) throw new GlobalException(STATION_NOT_FOUND);

        Place randomPlace = allPlaces.get((int) (Math.random() * allPlaces.size()));
        log.info("[NOTIFICATION TEST] 선택된 장소: placeId={}, name={}", randomPlace.getId(), randomPlace.getName());

        LocalDateTime now                = LocalDateTime.now();
        LocalDateTime trainDepartureTime = now.plusMinutes(remainingMinutes);

        VisitingHistory testHistory = VisitingHistory.of(
                user,
                randomStation,
                randomPlace,
                now,
                trainDepartureTime
        );

        VisitingHistory savedHistory = visitingHistoryRepository.save(testHistory);
        log.info("[NOTIFICATION TEST] 테스트 여정 생성: historyId={}", savedHistory.getId());

        eventPublisher.publishEvent(new JourneyStartedEvent(
                userId,
                savedHistory.getId(),
                trainDepartureTime
        ));

        log.info("[NOTIFICATION TEST] 완료: historyId={}, notificationScheduled=true", savedHistory.getId());

        return new TestNotificationResponse(
                savedHistory.getId(),
                randomStation.getName(),
                randomPlace.getName(),
                trainDepartureTime,
                remainingMinutes,
                true,
                notificationTimings
        );
    }

}
