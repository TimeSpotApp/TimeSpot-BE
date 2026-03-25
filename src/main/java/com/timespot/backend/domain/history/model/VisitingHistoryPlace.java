package com.timespot.backend.domain.history.model;

import static com.timespot.backend.common.response.ErrorCode.HISTORY_INVALID_PLACE;
import static com.timespot.backend.common.response.ErrorCode.HISTORY_INVALID_VISIT_HISTORY;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.model.BaseAuditingEntity;
import com.timespot.backend.domain.place.model.Place;
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
 * PackageName : com.timespot.backend.domain.history.model
 * FileName    : VisitingHistoryPlace
 * Author      : loadingKKamo21
 * Date        : 26. 3. 24.
 * Description : 방문 이력 - 장소 매핑 엔티티 (여정 내 방문한 장소 목록)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 24.    loadingKKamo21       Initial creation
 */
@Entity
@Table(name = "visiting_history_places")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class VisitingHistoryPlace extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "visiting_history_place_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "visiting_history_id", nullable = false)
    private VisitingHistory visitingHistory;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Builder(access = PRIVATE)
    private VisitingHistoryPlace(final VisitingHistory visitingHistory,
                                 final Place place) {
        validateVisitingHistory(visitingHistory);
        validatePlace(place);
        this.visitingHistory = visitingHistory;
        this.place = place;
    }

    // ========================= 생성자 메서드 =========================

    /**
     * 방문 장소 생성
     *
     * @param visitingHistory 방문 이력
     * @param place           장소
     * @return VisitingHistoryPlace 엔티티
     */
    public static VisitingHistoryPlace of(final VisitingHistory visitingHistory,
                                          final Place place) {
        return VisitingHistoryPlace.builder()
                                   .visitingHistory(visitingHistory)
                                   .place(place)
                                   .build();
    }

    // ========================= 검증 메서드 =========================

    /**
     * 방문 이력 검증
     *
     * @param visitingHistory 방문 이력
     */
    private void validateVisitingHistory(final VisitingHistory visitingHistory) {
        if (visitingHistory == null || visitingHistory.getId() == null)
            throw new GlobalException(HISTORY_INVALID_VISIT_HISTORY);
    }

    /**
     * 장소 검증
     *
     * @param place 장소
     */
    private void validatePlace(final Place place) {
        if (place == null || place.getId() == null) throw new GlobalException(HISTORY_INVALID_PLACE);
    }

}
