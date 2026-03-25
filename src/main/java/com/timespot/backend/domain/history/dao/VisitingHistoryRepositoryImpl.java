package com.timespot.backend.domain.history.dao;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.timespot.backend.domain.history.model.QVisitingHistory;
import com.timespot.backend.domain.history.model.QVisitingHistoryPlace;
import com.timespot.backend.domain.user.model.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * PackageName : com.timespot.backend.domain.history.dao
 * FileName    : VisitingHistoryRepositoryImpl
 * Author      : loadingKKamo21
 * Date        : 26. 3. 24.
 * Description : 방문 이력 리포지토리 구현체 (QueryDSL)
 */
@Repository
@RequiredArgsConstructor
public class VisitingHistoryRepositoryImpl implements VisitingHistoryRepositoryCustom {

    private static final QUser                 USER                   = QUser.user;
    private static final QVisitingHistory      VISITING_HISTORY       = QVisitingHistory.visitingHistory;
    private static final QVisitingHistoryPlace VISITING_HISTORY_PLACE = QVisitingHistoryPlace.visitingHistoryPlace;

    private final JPAQueryFactory queryFactory;

}
