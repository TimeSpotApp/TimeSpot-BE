package com.timespot.backend.domain.history.model;

import static com.timespot.backend.common.response.ErrorCode.HISTORY_INVALID_END_TIME;
import static com.timespot.backend.common.response.ErrorCode.HISTORY_INVALID_START_TIME;
import static com.timespot.backend.common.response.ErrorCode.HISTORY_INVALID_USER;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.model.BaseAuditingEntity;
import com.timespot.backend.domain.place.model.Station;
import com.timespot.backend.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.history.model
 * FileName    : VisitingHistory
 * Author      : loadingKKamo21
 * Date        : 26. 3. 24.
 * Description : 방문 이력 엔티티 (사용자-역 여정 기록)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 24.    loadingKKamo21       Initial creation
 */
@Entity
@Table(name = "visiting_histories")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class VisitingHistory extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "visiting_history_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "train_departure_time", nullable = false)
    private LocalDateTime trainDepartureTime;

    @Column(name = "total_duration_minutes")
    private Integer totalDurationMinutes;

    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess;

    @Column(name = "memo", length = 500)
    private String memo;

    @Builder(access = PRIVATE)
    private VisitingHistory(final User user,
                            final Station station,
                            final LocalDateTime startTime,
                            final LocalDateTime trainDepartureTime,
                            final LocalDateTime endTime,
                            final Boolean isSuccess,
                            final String memo) {
        validateUser(user);
        validateStation(station);
        validateStartTime(startTime);
        validateTrainDepartureTime(trainDepartureTime);
        validateEndTime(startTime, endTime);
        this.user = user;
        this.station = station;
        this.startTime = startTime;
        this.trainDepartureTime = trainDepartureTime;
        this.endTime = endTime;
        this.isSuccess = isSuccess != null ? isSuccess : false;
        this.totalDurationMinutes = calculateDurationMinutes(startTime, endTime);
        this.memo = memo;
    }

    // ========================= 생성자 메서드 =========================

    /**
     * 여정 시작 (탐색 시작 시점, 종료 시간 없음)
     *
     * @param user               사용자
     * @param station            역
     * @param startTime          탐색 시작 시간 (현재 시간)
     * @param trainDepartureTime 열차 출발 시간
     * @return VisitingHistory 엔티티
     */
    public static VisitingHistory startJourney(final User user,
                                               final Station station,
                                               final LocalDateTime startTime,
                                               final LocalDateTime trainDepartureTime) {
        return VisitingHistory.builder()
                              .user(user)
                              .station(station)
                              .startTime(startTime)
                              .trainDepartureTime(trainDepartureTime)
                              .endTime(null)
                              .isSuccess(false)
                              .memo(null)
                              .build();
    }

    /**
     * 방문 이력 생성 (탐색 시작 및 종료 시간 모두 설정)
     *
     * @param user               사용자
     * @param station            역
     * @param startTime          탐색 시작 시간
     * @param endTime            탐색 종료 시간
     * @param trainDepartureTime 열차 출발 시간
     * @param memo               메모
     * @return VisitingHistory 엔티티
     */
    public static VisitingHistory of(final User user,
                                     final Station station,
                                     final LocalDateTime startTime,
                                     final LocalDateTime endTime,
                                     final LocalDateTime trainDepartureTime,
                                     final String memo) {
        return VisitingHistory.builder()
                              .user(user)
                              .station(station)
                              .startTime(startTime)
                              .trainDepartureTime(trainDepartureTime)
                              .endTime(endTime)
                              .isSuccess(!endTime.isAfter(trainDepartureTime))
                              .memo(memo)
                              .build();
    }

    // ========================= 검증 메서드 =========================

    /**
     * 사용자 검증
     *
     * @param user 사용자
     */
    private void validateUser(final User user) {
        if (user == null || user.getId() == null || user.getCreatedAt() == null)
            throw new GlobalException(HISTORY_INVALID_USER);
    }

    /**
     * 역 검증
     *
     * @param station 역
     */
    private void validateStation(final Station station) {
        if (station == null || station.getId() == null)
            throw new GlobalException(HISTORY_INVALID_START_TIME);
    }

    /**
     * 시작 시간 검증
     *
     * @param startTime 시작 시간
     */
    private void validateStartTime(final LocalDateTime startTime) {
        if (startTime == null) throw new GlobalException(HISTORY_INVALID_START_TIME);
    }

    /**
     * 열차 출발 시간 검증
     *
     * @param trainDepartureTime 열차 출발 시간
     */
    private void validateTrainDepartureTime(final LocalDateTime trainDepartureTime) {
        if (trainDepartureTime == null) throw new GlobalException(HISTORY_INVALID_START_TIME);
    }

    /**
     * 종료 시간 검증 (시작 시간보다 이후여야 함)
     *
     * @param startTime 시작 시간
     * @param endTime   종료 시간
     */
    private void validateEndTime(final LocalDateTime startTime, final LocalDateTime endTime) {
        if (endTime != null && (endTime.isBefore(startTime) || endTime.isEqual(startTime)))
            throw new GlobalException(HISTORY_INVALID_END_TIME);
    }

    // ========================= 비즈니스 메서드 =========================

    /**
     * 탐색 종료 (종료 시간 설정 및 성공/실패 자동 판별)
     *
     * @param endTime 탐색 종료 시간
     */
    public void endJourney(final LocalDateTime endTime) {
        validateEndTime(this.startTime, endTime);
        this.endTime = endTime;
        this.totalDurationMinutes = calculateDurationMinutes(this.startTime, endTime);
        this.isSuccess = !endTime.isAfter(this.trainDepartureTime);
    }

    /**
     * 여정 포기 (사용자가 중도 포기)
     */
    public void abandonJourney() {
        this.endTime = null;
        this.isSuccess = false;
    }

    /**
     * 메모 업데이트
     *
     * @param newMemo 새로운 메모
     */
    public void updateMemo(final String newMemo) {
        this.memo = newMemo;
    }

    /**
     * 소요 시간 (분) 계산
     *
     * @param start 시작 시간
     * @param end   종료 시간
     * @return 소요 시간 (분), 종료 시간이 없으면 0
     */
    private int calculateDurationMinutes(final LocalDateTime start, final LocalDateTime end) {
        if (end == null) return 0;
        return (int) Duration.between(start, end).toMinutes();
    }

    /**
     * 진행 중 여부 확인
     *
     * @return 진행 중이면 true
     */
    public boolean isInProgress() {
        return this.endTime == null;
    }

    /**
     * 여정 성공 여부 확인
     *
     * @return 성공이면 true
     */
    public boolean isSuccess() {
        return this.isSuccess != null && this.isSuccess;
    }

    /**
     * 총 여정 시간 (분) 반환
     *
     * @return 총 여정 시간 (분)
     */
    public int getTotalDurationMinutes() {
        return this.totalDurationMinutes != null ? this.totalDurationMinutes : 0;
    }

}
