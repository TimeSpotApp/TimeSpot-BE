package com.timespot.backend.domain.favorite.model;

import static com.timespot.backend.common.response.ErrorCode.FAVORITE_INVALID_STATION;
import static com.timespot.backend.common.response.ErrorCode.FAVORITE_INVALID_USER;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.favorite.model
 * FileName    : Favorite
 * Author      : loadingKKamo21
 * Date        : 26. 3. 24.
 * Description : 즐겨찾기 엔티티 (사용자 -역 즐겨찾기)
 *
 * ## 방문 통계
 * - `visitCount`: 정상 완료된 여정 횟수 (포기/시간 초과 제외)
 * - `totalVisitMinutes`: 정상 완료된 여정의 총 소요 시간 (분)
 *
 * ## 통계 업데이트 시점
 * - `addVisitHistory(durationMinutes)`: 여정이 정상 완료된 경우 자동 호출
 * - 사용자 통계 (`User.totalVisitCount`, `User.totalJourneyMinutes`) 와 동기화
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 24.    loadingKKamo21       Initial creation
 * 26. 3. 26.    loadingKKamo21       방문 횟수 + 누적 시간 필드 추가, 통계 업데이트 로직 추가
 */
@Entity
@Table(name = "favorites")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class Favorite extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "favorite_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(name = "visit_count", nullable = false)
    private Integer visitCount;

    @Column(name = "total_visit_minutes", nullable = false)
    private Integer totalVisitMinutes;

    @Builder(access = PRIVATE)
    private Favorite(final User user,
                     final Station station,
                     final Integer visitCount,
                     final Integer totalVisitMinutes) {
        validateUser(user);
        validateStation(station);
        this.user = user;
        this.station = station;
        this.visitCount = visitCount != null ? visitCount : 0;
        this.totalVisitMinutes = totalVisitMinutes != null ? totalVisitMinutes : 0;
    }

    // ========================= 생성자 메서드 =========================

    /**
     * 즐겨찾기 생성 (기본 정보만)
     *
     * @param user    사용자
     * @param station 역
     * @return Favorite 엔티티
     */
    public static Favorite of(final User user,
                              final Station station) {
        return Favorite.builder()
                       .user(user)
                       .station(station)
                       .visitCount(0)
                       .totalVisitMinutes(0)
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
            throw new GlobalException(FAVORITE_INVALID_USER);
    }

    /**
     * 역 검증
     *
     * @param station 역
     */
    private void validateStation(final Station station) {
        if (station == null || station.getId() == null)
            throw new GlobalException(FAVORITE_INVALID_STATION);
    }

    // ========================= 비즈니스 메서드 =========================

    /**
     * 방문 횟수 증가
     */
    public void incrementVisitCount() {
        this.visitCount++;
    }

    /**
     * 방문 횟수 감소
     */
    public void decrementVisitCount() {
        if (this.visitCount > 0) this.visitCount--;
    }

    /**
     * 방문 이력 추가 (방문 횟수 + 누적 시간 업데이트)
     * <p>
     * - 여정이 정상 완료된 경우에만 호출됨
     * - 방문 횟수와 누적 시간을 모두 증가시킴
     * </p>
     *
     * @param durationMinutes 여정 시간 (분)
     */
    public void addVisitHistory(final int durationMinutes) {
        this.visitCount = this.visitCount + 1;
        this.totalVisitMinutes = this.totalVisitMinutes + durationMinutes;
    }

    /**
     * 총 방문 시간 (분) 반환
     *
     * @return 총 방문 시간 (분)
     */
    public int getTotalVisitMinutes() {
        return this.totalVisitMinutes != null ? this.totalVisitMinutes : 0;
    }

}
