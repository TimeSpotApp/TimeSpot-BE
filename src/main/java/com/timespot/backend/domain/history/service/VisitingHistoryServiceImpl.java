package com.timespot.backend.domain.history.service;

import static com.timespot.backend.common.response.ErrorCode.HISTORY_NOT_FOUND;
import static com.timespot.backend.common.response.ErrorCode.PLACE_NOT_FOUND;
import static com.timespot.backend.common.response.ErrorCode.STATION_NOT_FOUND;
import static com.timespot.backend.common.response.ErrorCode.USER_NOT_FOUND;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.domain.history.dao.VisitingHistoryRepository;
import com.timespot.backend.domain.history.dto.VisitingHistoryRequestDto.JourneyEndRequest;
import com.timespot.backend.domain.history.dto.VisitingHistoryRequestDto.JourneyStartRequest;
import com.timespot.backend.domain.history.dto.VisitingHistoryResponseDto.VisitingHistoryDetailResponse;
import com.timespot.backend.domain.history.dto.VisitingHistoryResponseDto.VisitingHistoryListResponse;
import com.timespot.backend.domain.history.model.VisitingHistory;
import com.timespot.backend.domain.place.dao.PlaceRepository;
import com.timespot.backend.domain.place.model.Place;
import com.timespot.backend.domain.station.dao.StationRepository;
import com.timespot.backend.domain.station.model.Station;
import com.timespot.backend.domain.user.dao.UserRepository;
import com.timespot.backend.domain.user.model.User;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
 * 26. 3. 25.    loadingKKamo21               Initial creation
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VisitingHistoryServiceImpl implements VisitingHistoryService {

    private final VisitingHistoryRepository visitingHistoryRepository;
    private final UserRepository            userRepository;
    private final StationRepository         stationRepository;
    private final PlaceRepository           placeRepository;

    @Override
    @Transactional
    public VisitingHistoryDetailResponse createNewJourney(final UUID userId, final JourneyStartRequest dto) {
        User    user    = getUserById(userId);
        Station station = getStationById(dto.getStationId());
        Place   place   = getPlaceById(dto.getPlaceId());

        VisitingHistory visitingHistory = visitingHistoryRepository.save(
                VisitingHistory.of(user, station, place, LocalDateTime.now(), dto.getTrainDepartureTime())
        );

        return VisitingHistoryDetailResponse.from(visitingHistory);
    }

    @Override
    @Transactional
    public VisitingHistoryDetailResponse endJourney(final UUID userId,
                                                    final Long historyId,
                                                    final JourneyEndRequest dto) {
        VisitingHistory visitingHistory = visitingHistoryRepository.findById(historyId)
                                                                   .orElseThrow(
                                                                           () -> new GlobalException(HISTORY_NOT_FOUND)
                                                                   );

        if (!visitingHistory.getUser().getId().equals(userId)) throw new GlobalException(HISTORY_NOT_FOUND);

        if (dto.getIsCompleted()) {
            visitingHistory.endJourney(LocalDateTime.now());

            User user = visitingHistory.getUser();
            if (visitingHistory.isSuccess())
                user.addVisitHistory(visitingHistory.getTotalDurationMinutes(), true);
        } else visitingHistory.abandonJourney();

        return VisitingHistoryDetailResponse.from(visitingHistory);
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

        visitingHistoryRepository.delete(visitingHistory);
    }

    // ========================= 내부 메서드 =========================

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
     * 장소 ID 로 장소 조회
     *
     * @param placeId 장소 ID
     * @return 장소 엔티티
     * @throws GlobalException 장소를 찾을 수 없는 경우
     */
    private Place getPlaceById(final Long placeId) {
        return placeRepository.findById(placeId).orElseThrow(() -> new GlobalException(PLACE_NOT_FOUND));
    }

}
