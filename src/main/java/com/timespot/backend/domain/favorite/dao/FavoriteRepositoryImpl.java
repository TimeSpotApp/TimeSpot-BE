package com.timespot.backend.domain.favorite.dao;

import static com.querydsl.core.types.Order.ASC;
import static com.querydsl.core.types.Order.DESC;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.timespot.backend.domain.favorite.dto.FavoriteResponseDto.FavoriteListResponse;
import com.timespot.backend.domain.favorite.dto.QFavoriteResponseDto_FavoriteListResponse;
import com.timespot.backend.domain.favorite.model.QFavorite;
import com.timespot.backend.domain.station.model.QStation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * PackageName : com.timespot.backend.domain.favorite.dao
 * FileName    : FavoriteRepositoryImpl
 * Author      : loadingKKamo21
 * Date        : 26. 3. 24.
 * Description : 즐겨찾기 리포지토리 구현체 (QueryDSL + 성능 최적화)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 24.    loadingKKamo21       Initial creation
 * 26. 3. 24.    loadingKKamo21       성능 최적화 (불필요한 JOIN 제거, 2 단계 필터링 강화)
 */
@Repository
@RequiredArgsConstructor
public class FavoriteRepositoryImpl implements FavoriteRepositoryCustom {

    private static final QFavorite FAVORITE = QFavorite.favorite;
    private static final QStation  STATION  = QStation.station;

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<FavoriteListResponse> findFavoriteStationList(final UUID userId,
                                                              final String keyword,
                                                              final Pageable pageable) {
        final List<Long> favoriteIds = findFavoriteIds(userId, keyword, pageable);

        if (favoriteIds.isEmpty()) return Page.empty(pageable);

        final List<FavoriteListResponse> content = queryFactory.select(
                                                                       new QFavoriteResponseDto_FavoriteListResponse(
                                                                               FAVORITE.id,
                                                                               FAVORITE.station.id,
                                                                               FAVORITE.station.name,
                                                                               FAVORITE.visitCount,
                                                                               FAVORITE.createdAt
                                                                       )
                                                               )
                                                               .from(FAVORITE)
                                                               .join(STATION).on(FAVORITE.station.id.eq(STATION.id))
                                                               .where(FAVORITE.id.in(favoriteIds),
                                                                      stationNameContains(keyword))
                                                               .orderBy(getSortCondition(pageable))
                                                               .fetch();

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                () -> queryFactory.select(FAVORITE.count())
                                  .from(FAVORITE)
                                  .where(FAVORITE.user.id.eq(userId),
                                         stationNameContains(keyword))
                                  .fetchOne()
        );
    }

    // ========================= 내부 메서드 =========================

    /**
     * 즐겨찾기 ID 목록 조회 (Index Only Scan)
     *
     * @param userId   사용자 ID
     * @param keyword  검색어 (역 이름)
     * @param pageable 페이지네이션 정보
     * @return 즐겨찾기 ID 목록
     */
    private List<Long> findFavoriteIds(final UUID userId, final String keyword, final Pageable pageable) {
        return queryFactory.select(FAVORITE.id)
                           .from(FAVORITE)
                           .where(FAVORITE.user.id.eq(userId),
                                  stationNameContains(keyword))
                           .orderBy(getSortCondition(pageable))
                           .offset(pageable.getOffset())
                           .limit(pageable.getPageSize())
                           .fetch();
    }

    /**
     * 역 이름 LIKE 검색 조건
     *
     * @param keyword 검색어 (역 이름)
     * @return LIKE 조건식 (null 이면 필터링 안함)
     */
    private BooleanExpression stationNameContains(final String keyword) {
        if (!StringUtils.hasText(keyword)) return null;
        return FAVORITE.station.name.containsIgnoreCase(keyword.trim());
    }

    /**
     * 정렬 조건 생성
     *
     * @param pageable 페이지네이션 정보
     * @return 정렬 조건 배열
     */
    private OrderSpecifier<?>[] getSortCondition(final Pageable pageable) {
        if (pageable.getSort().isEmpty()) return new OrderSpecifier[]{new OrderSpecifier<>(DESC, FAVORITE.createdAt)};

        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        pageable.getSort().forEach(order -> {
            Order  direction = order.getDirection().isAscending() ? ASC : DESC;
            String property  = order.getProperty();

            switch (property) {
                case "stationName" -> orderSpecifiers.add(new OrderSpecifier<>(direction, FAVORITE.station.name));
                case "visitCount" -> orderSpecifiers.add(new OrderSpecifier<>(direction, FAVORITE.visitCount));
                case "createdAt" -> {
                    orderSpecifiers.add(new OrderSpecifier<>(direction, FAVORITE.createdAt));
                    orderSpecifiers.add(new OrderSpecifier<>(direction, FAVORITE.id));
                }
                default -> {
                    orderSpecifiers.add(new OrderSpecifier<>(DESC, FAVORITE.createdAt));
                    orderSpecifiers.add(new OrderSpecifier<>(DESC, FAVORITE.id));
                }
            }
        });

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }

}
