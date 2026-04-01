package com.timespot.backend.domain.history.model;

import static com.timespot.backend.common.response.ErrorCode.HISTORY_ALREADY_ENDED;
import static com.timespot.backend.common.response.ErrorCode.HISTORY_INVALID_END_TIME;
import static com.timespot.backend.common.response.ErrorCode.HISTORY_INVALID_PLACE;
import static com.timespot.backend.common.response.ErrorCode.HISTORY_INVALID_START_TIME;
import static com.timespot.backend.common.response.ErrorCode.HISTORY_INVALID_STATION;
import static com.timespot.backend.common.response.ErrorCode.HISTORY_INVALID_USER;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.model.BaseAuditingEntity;
import com.timespot.backend.domain.station.model.Station;
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
import org.locationtech.jts.geom.Point;

/**
 * PackageName : com.timespot.backend.domain.history.model
 * FileName    : VisitingHistory
 * Author      : loadingKKamo21
 * Date        : 26. 3. 24.
 * Description : 방문 이력 엔티티 (사용자-역 여정 기록)
 * <p>
 * ## 여정 상태 흐름
 * 1. **여정 시작**: `of(user, station, placeContentId, ...)` - 진행 중 상태
 * 2. **여정 종료**:
 * - 정상 완료: `endJourney(endTime)` - `isSuccess = true` (열차 출발 전 도착)
 * - 시간 초과: `endJourney(endTime)` - `isSuccess = false` (열차 출발 후 도착)
 * - 중도 포기: `abandonJourney()` - `isSuccess = false`, `endTime = null`
 * 3. **접근 차단**: `validateEndable()` - 이미 종료된 이력에 대한 재접근 차단
 * <p>
 * ## 통계 업데이트
 * - 여정이 **정상 완료**된 경우 (`isSuccess == true`) 에만 사용자 통계 및 즐겨찾기 업데이트
 * - 포기 또는 시간 초과한 경우 통계 미반영
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 24.    loadingKKamo21       Initial creation
 * 26. 3. 26.    loadingKKamo21       이미 종료된 이력 접근 차단 로직 추가, 통계 업데이트 로직 강화
 * 26. 4. 1.     loadingKKamo21       Place 엔티티 참조 제거, 직접 컬럼 저장 (place_content_id 등)
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

    @Column(name = "place_content_id", nullable = false)
    private String placeContentId;

    @Column(name = "place_name", nullable = false)
    private String placeName;

    @Column(name = "place_category", nullable = false)
    private String placeCategory;

    @Column(name = "place_address", nullable = false)
    private String placeAddress;

    @Column(name = "place_location", columnDefinition = "POINT SRID 4326", nullable = false)
    private Point placeLocation;

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

    @Builder(access = PRIVATE)
    private VisitingHistory(final User user,
                            final Station station,
                            final String placeContentId,
                            final String placeName,
                            final String placeCategory,
                            final String placeAddress,
                            final Point placeLocation,
                            final LocalDateTime startTime,
                            final LocalDateTime trainDepartureTime,
                            final LocalDateTime endTime,
                            final Boolean isSuccess) {
        validateUser(user);
        validateStation(station);
        validatePlaceContentId(placeContentId);
        validatePlaceName(placeName);
        validateStartTime(startTime);
        validateTrainDepartureTime(trainDepartureTime);
        validateEndTime(startTime, endTime);
        this.user = user;
        this.station = station;
        this.placeContentId = placeContentId;
        this.placeName = placeName;
        this.placeCategory = placeCategory;
        this.placeAddress = placeAddress;
        this.placeLocation = placeLocation;
        this.startTime = startTime;
        this.trainDepartureTime = trainDepartureTime;
        this.endTime = endTime;
        this.isSuccess = isSuccess != null ? isSuccess : false;
        this.totalDurationMinutes = calculateDurationMinutes(startTime, endTime);
    }

    // ========================= 생성자 메서드 =========================

    /**
     * 여정 시작 (탐색 시작 시점, 종료 시간 없음)
     *
     * @param user               사용자
     * @param station            역
     * @param placeContentId     한국관광공사 contentId
     * @param placeName          장소 이름
     * @param placeCategory      장소 카테고리
     * @param placeAddress       장소 주소
     * @param placeLocation      장소 위치 (POINT)
     * @param startTime          탐색 시작 시간 (현재 시간)
     * @param trainDepartureTime 열차 출발 시간
     * @return VisitingHistory 엔티티
     */
    public static VisitingHistory of(final User user,
                                     final Station station,
                                     final String placeContentId,
                                     final String placeName,
                                     final String placeCategory,
                                     final String placeAddress,
                                     final Point placeLocation,
                                     final LocalDateTime startTime,
                                     final LocalDateTime trainDepartureTime) {
        return VisitingHistory.builder()
                              .user(user)
                              .station(station)
                              .placeContentId(placeContentId)
                              .placeName(placeName)
                              .placeCategory(placeCategory)
                              .placeAddress(placeAddress)
                              .placeLocation(placeLocation)
                              .startTime(startTime)
                              .trainDepartureTime(trainDepartureTime)
                              .endTime(null)
                              .isSuccess(false)
                              .build();
    }

    /**
     * 방문 이력 생성 (탐색 시작 및 종료 시간 모두 설정)
     *
     * @param user               사용자
     * @param station            역
     * @param placeContentId     한국관광공사 contentId
     * @param placeName          장소 이름
     * @param placeCategory      장소 카테고리
     * @param placeAddress       장소 주소
     * @param placeLocation      장소 위치 (POINT)
     * @param startTime          탐색 시작 시간
     * @param endTime            탐색 종료 시간
     * @param trainDepartureTime 열차 출발 시간
     * @return VisitingHistory 엔티티
     */
    public static VisitingHistory of(final User user,
                                     final Station station,
                                     final String placeContentId,
                                     final String placeName,
                                     final String placeCategory,
                                     final String placeAddress,
                                     final Point placeLocation,
                                     final LocalDateTime startTime,
                                     final LocalDateTime endTime,
                                     final LocalDateTime trainDepartureTime) {
        return VisitingHistory.builder()
                              .user(user)
                              .station(station)
                              .placeContentId(placeContentId)
                              .placeName(placeName)
                              .placeCategory(placeCategory)
                              .placeAddress(placeAddress)
                              .placeLocation(placeLocation)
                              .startTime(startTime)
                              .trainDepartureTime(trainDepartureTime)
                              .endTime(endTime)
                              .isSuccess(endTime != null && !endTime.isAfter(trainDepartureTime))
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
            throw new GlobalException(HISTORY_INVALID_STATION);
    }

    /**
     * 장소 검증
     *
     * @param placeContentId 한국관광공사 장소 고유 식별자
     */
    private void validatePlaceContentId(final String placeContentId) {
        if (placeContentId == null || placeContentId.isBlank()) throw new GlobalException(HISTORY_INVALID_PLACE);
    }

    /**
     * 장소 이름 검증
     *
     * @param placeName 장소 이름
     */
    private void validatePlaceName(final String placeName) {
        if (placeName == null || placeName.isBlank()) throw new GlobalException(HISTORY_INVALID_PLACE);
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
        return this.endTime == null && (this.isSuccess == null || !this.isSuccess);
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

    /**
     * 여정 종료 검증 (이미 종료된 이력에 대한 재접근 차단)
     *
     * @throws GlobalException HISTORY_ALREADY_ENDED
     */
    public void validateEndable() {
        if (!isInProgress()) throw new GlobalException(HISTORY_ALREADY_ENDED);
    }

}
