package com.timespot.backend.domain.history.dao;

import static com.querydsl.core.types.Order.ASC;
import static com.querydsl.core.types.Order.DESC;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.timespot.backend.domain.history.dto.QVisitingHistoryResponseDto_VisitingHistoryDetailResponse;
import com.timespot.backend.domain.history.dto.QVisitingHistoryResponseDto_VisitingHistoryListResponse;
import com.timespot.backend.domain.history.dto.VisitingHistoryResponseDto.VisitingHistoryDetailResponse;
import com.timespot.backend.domain.history.dto.VisitingHistoryResponseDto.VisitingHistoryListResponse;
import com.timespot.backend.domain.history.model.QVisitingHistory;
import com.timespot.backend.domain.station.model.QStation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * PackageName : com.timespot.backend.domain.history.dao
 * FileName    : VisitingHistoryRepositoryImpl
 * Author      : loadingKKamo21
 * Date        : 26. 3. 24.
 * Description : 방문 이력 리포지토리 구현체 (QueryDSL)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 24.    loadingKKamo21       Initial creation
 * 26. 4. 1.     loadingKKamo21       Place 엔티티 조인 제거, 직접 컬럼 사용
 */
@Repository
@RequiredArgsConstructor
public class VisitingHistoryRepositoryImpl implements VisitingHistoryRepositoryCustom {

    private static final QVisitingHistory VISITING_HISTORY = QVisitingHistory.visitingHistory;
    private static final QStation         STATION          = QStation.station;

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<VisitingHistoryDetailResponse> findVisitingHistoryDetail(final UUID userId, final Long historyId) {
        return Optional.ofNullable(queryFactory.select(
                                                       new QVisitingHistoryResponseDto_VisitingHistoryDetailResponse(
                                                               VISITING_HISTORY.id,
                                                               STATION.id,
                                                               STATION.name,
                                                               STATION.address,
                                                               VISITING_HISTORY.placeContentId,
                                                               VISITING_HISTORY.placeName,
                                                               VISITING_HISTORY.placeCategory,
                                                               VISITING_HISTORY.placeAddress,
                                                               VISITING_HISTORY.placeLocation,
                                                               VISITING_HISTORY.startTime,
                                                               VISITING_HISTORY.endTime,
                                                               VISITING_HISTORY.trainDepartureTime,
                                                               VISITING_HISTORY.totalDurationMinutes,
                                                               VISITING_HISTORY.isSuccess.isNull().or(VISITING_HISTORY.isSuccess.isFalse()),
                                                               VISITING_HISTORY.isSuccess,
                                                               VISITING_HISTORY.createdAt
                                                       )
                                               )
                                               .from(VISITING_HISTORY)
                                               .join(STATION).on(VISITING_HISTORY.station.id.eq(STATION.id))
                                               .where(VISITING_HISTORY.user.id.eq(userId),
                                                      VISITING_HISTORY.id.eq(historyId))
                                               .fetchOne());
    }

    @Override
    public Page<VisitingHistoryListResponse> findVisitingHistoryList(final UUID userId,
                                                                     final String keyword,
                                                                     final Pageable pageable) {
        final List<Long> historyIds = findVisitingHistoryIds(userId, keyword, pageable);

        if (historyIds.isEmpty()) return Page.empty(pageable);

        final List<VisitingHistoryListResponse> content = queryFactory.select(
                                                                              new QVisitingHistoryResponseDto_VisitingHistoryListResponse(
                                                                                      VISITING_HISTORY.id,
                                                                                      STATION.id,
                                                                                      STATION.name,
                                                                                      VISITING_HISTORY.placeContentId,
                                                                                      VISITING_HISTORY.placeName,
                                                                                      VISITING_HISTORY.placeCategory,
                                                                                      VISITING_HISTORY.startTime,
                                                                                      VISITING_HISTORY.endTime,
                                                                                      VISITING_HISTORY.trainDepartureTime,
                                                                                      VISITING_HISTORY.totalDurationMinutes,
                                                                                      VISITING_HISTORY.isSuccess.isNull().or(VISITING_HISTORY.isSuccess.isFalse()),
                                                                                      VISITING_HISTORY.isSuccess,
                                                                                      VISITING_HISTORY.createdAt
                                                                              )
                                                                      )
                                                                      .from(VISITING_HISTORY)
                                                                      .join(STATION)
                                                                      .on(VISITING_HISTORY.station.id.eq(STATION.id))
                                                                      .where(VISITING_HISTORY.id.in(historyIds),
                                                                             stationOrPlaceContains(keyword))
                                                                      .orderBy(getSortCondition(pageable))
                                                                      .offset(pageable.getOffset())
                                                                      .limit(pageable.getPageSize())
                                                                      .fetch();

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                () -> queryFactory.select(VISITING_HISTORY.count())
                                  .from(VISITING_HISTORY)
                                  .where(VISITING_HISTORY.user.id.eq(userId),
                                         stationOrPlaceContains(keyword))
                                  .fetchOne()
        );
    }

    // ========================= 내부 메서드 =========================

    private List<Long> findVisitingHistoryIds(final UUID userId, final String keyword, final Pageable pageable) {
        return queryFactory.select(VISITING_HISTORY.id)
                           .from(VISITING_HISTORY)
                           .where(VISITING_HISTORY.user.id.eq(userId),
                                  stationOrPlaceContains(keyword))
                           .orderBy(getSortCondition(pageable))
                           .offset(pageable.getOffset())
                           .limit(pageable.getPageSize())
                           .fetch();
    }

    private BooleanExpression stationOrPlaceContains(final String keyword) {
        if (!StringUtils.hasText(keyword)) return null;
        return STATION.name.containsIgnoreCase(keyword.trim())
                           .or(STATION.address.containsIgnoreCase(keyword.trim()))
                           .or(VISITING_HISTORY.placeName.containsIgnoreCase(keyword.trim()))
                           .or(VISITING_HISTORY.placeAddress.containsIgnoreCase(keyword.trim()));
    }

    private OrderSpecifier<?>[] getSortCondition(final Pageable pageable) {
        if (pageable.getSort().isEmpty())
            return new OrderSpecifier[]{new OrderSpecifier<>(DESC, VISITING_HISTORY.createdAt),
                                        new OrderSpecifier<>(DESC, VISITING_HISTORY.id)};

        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        pageable.getSort().forEach(order -> {
            Order  direction = order.getDirection().isAscending() ? ASC : DESC;
            String property  = order.getProperty();

            switch (property) {
                case "duration" -> orderSpecifiers.add(new OrderSpecifier<>(
                        direction,
                        Expressions.numberTemplate(Long.class,
                                                   "TIMESTAMPDIFF(SECOND, {0}, {1})",
                                                   VISITING_HISTORY.startTime,
                                                   VISITING_HISTORY.endTime)
                ));
                case "createdAt" -> {
                    orderSpecifiers.add(new OrderSpecifier<>(direction, VISITING_HISTORY.createdAt));
                    orderSpecifiers.add(new OrderSpecifier<>(direction, VISITING_HISTORY.id));
                }
                default -> {
                    orderSpecifiers.add(new OrderSpecifier<>(DESC, VISITING_HISTORY.createdAt));
                    orderSpecifiers.add(new OrderSpecifier<>(DESC, VISITING_HISTORY.id));
                }
            }
        });

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }

}
