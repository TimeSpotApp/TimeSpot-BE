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
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 24.    loadingKKamo21       Initial creation
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

    @Column(name = "memo", length = 500)
    private String memo;

    @Column(name = "visit_count", nullable = false)
    private Integer visitCount;

    @Builder(access = PRIVATE)
    private Favorite(final User user,
                     final Station station,
                     final String memo,
                     final Integer visitCount) {
        validateUser(user);
        validateStation(station);
        this.user = user;
        this.station = station;
        this.memo = memo;
        this.visitCount = visitCount != null ? visitCount : 0;
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
                       .build();
    }

    /**
     * 즐겨찾기 생성 (메모 포함)
     *
     * @param user    사용자
     * @param station 역
     * @param memo    메모
     * @return Favorite 엔티티
     */
    public static Favorite of(final User user,
                              final Station station,
                              final String memo) {
        return Favorite.builder()
                       .user(user)
                       .station(station)
                       .memo(memo)
                       .visitCount(0)
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
     * 메모 업데이트
     *
     * @param newMemo 새로운 메모
     */
    public void updateMemo(final String newMemo) {
        this.memo = newMemo;
    }

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

}
