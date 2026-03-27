package com.timespot.backend.domain.station.dao;

import static com.querydsl.core.types.Order.ASC;
import static com.querydsl.core.types.Order.DESC;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.timespot.backend.domain.favorite.model.QFavorite;
import com.timespot.backend.domain.station.dto.QStationResponseDto_StationListResponse;
import com.timespot.backend.domain.station.dto.StationResponseDto.StationListResponse;
import com.timespot.backend.domain.station.model.QStation;
import com.timespot.backend.domain.user.model.QUser;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * PackageName : com.timespot.backend.domain.station.dao
 * FileName    : StationRepositoryImpl
 * Author      : loadingKKamo21
 * Date        : 26. 3. 26.
 * Description : 역 리포지토리 커스텀 구현체 (Querydsl)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 26.    loadingKKamo21       Initial creation
 */
@Repository
@RequiredArgsConstructor
public class StationRepositoryImpl implements StationRepositoryCustom {

    private static final int FAVORITE_LIST_LIMIT = 5;
    private static final int NEARBY_LIST_LIMIT   = 5;

    private static final QStation  STATION  = QStation.station;
    private static final QUser     USER     = QUser.user;
    private static final QFavorite FAVORITE = QFavorite.favorite;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<StationListResponse> findFavoriteStationList(final UUID userId, final String keyword) {
        return queryFactory.select(
                                   new QStationResponseDto_StationListResponse(
                                           STATION.id,
                                           STATION.name,
                                           STATION.location,
                                           STATION.lineName
                                   )
                           )
                           .from(STATION)
                           .join(FAVORITE).on(STATION.id.eq(FAVORITE.station.id))
                           .where(FAVORITE.user.id.eq(userId),
                                  STATION.isActive.isTrue(),
                                  stationContains(keyword))
                           .orderBy(FAVORITE.visitCount.desc())
                           .limit(FAVORITE_LIST_LIMIT)
                           .fetch();
    }

    @Override
    public List<StationListResponse> findNearbyStationList(final Point point,
                                                           final double radius,
                                                           final String keyword) {
        NumberTemplate<Double> distanceExpression = getDistanceExpression(point);
        return queryFactory.select(
                                   new QStationResponseDto_StationListResponse(
                                           STATION.id,
                                           STATION.name,
                                           STATION.location,
                                           STATION.lineName
                                   )
                           )
                           .from(STATION)
                           .where(distanceExpression.loe(radius),
                                  STATION.isActive.isTrue(),
                                  stationContains(keyword))
                           .orderBy(distanceExpression.asc())
                           .limit(NEARBY_LIST_LIMIT)
                           .fetch();
    }

    @Override
    public Page<StationListResponse> findStationList(final String keyword, final Pageable pageable) {
        final List<Long> stationIds = findStationIds(keyword, pageable);

        if (stationIds.isEmpty()) return Page.empty(pageable);

        final List<StationListResponse> content = queryFactory.select(
                                                                      new QStationResponseDto_StationListResponse(
                                                                              STATION.id,
                                                                              STATION.name,
                                                                              STATION.location,
                                                                              STATION.lineName
                                                                      )
                                                              )
                                                              .from(STATION)
                                                              .where(STATION.id.in(stationIds),
                                                                     STATION.isActive.isTrue(),
                                                                     stationContains(keyword))
                                                              .orderBy(getSortCondition(pageable))
                                                              .offset(pageable.getOffset())
                                                              .limit(pageable.getPageSize())
                                                              .fetch();

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                () -> queryFactory.select(STATION.count())
                                  .from(STATION)
                                  .where(STATION.isActive.isTrue(),
                                         stationContains(keyword))
                                  .fetchOne()
        );
    }

    // ========================= 내부 메서드 =========================

    private List<Long> findStationIds(final String keyword, final Pageable pageable) {
        return queryFactory.select(STATION.id)
                           .from(STATION)
                           .where(STATION.isActive.isTrue(),
                                  stationContains(keyword))
                           .offset(pageable.getOffset())
                           .limit(pageable.getPageSize())
                           .fetch();
    }

    private NumberTemplate<Double> getDistanceExpression(final Point point) {
        return Expressions.numberTemplate(Double.class,
                                          "ST_Distance_Sphere({0}, ST_GeomFromText({1}, 4326))",
                                          STATION.location,
                                          Expressions.constant(
                                                  String.format("POINT(%f %f)", point.getY(), point.getX())
                                          ));
    }

    private BooleanExpression stationContains(final String keyword) {
        if (!StringUtils.hasText(keyword)) return null;
        return STATION.name.containsIgnoreCase(keyword.trim())
                           .or(STATION.address.containsIgnoreCase(keyword.trim()));
    }

    private OrderSpecifier<?>[] getSortCondition(final Pageable pageable) {
        if (pageable.getSort().isEmpty()) return new OrderSpecifier[]{
                new OrderSpecifier<>(ASC, STATION.name), new OrderSpecifier<>(ASC, STATION.id)
        };

        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        pageable.getSort().forEach(order -> {
            Order  direction = order.getDirection().isAscending() ? ASC : DESC;
            String property  = order.getProperty();

            switch (property) {
                case "stationName" -> {
                    orderSpecifiers.add(new OrderSpecifier<>(direction, STATION.name));
                    orderSpecifiers.add(new OrderSpecifier<>(direction, STATION.id));
                }
                default -> {
                    orderSpecifiers.add(new OrderSpecifier<>(ASC, STATION.name));
                    orderSpecifiers.add(new OrderSpecifier<>(ASC, STATION.id));
                }
            }
        });

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }

}
