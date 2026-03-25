package com.timespot.backend.domain.favorite.dao;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.timespot.backend.domain.favorite.model.QFavorite;
import com.timespot.backend.domain.user.model.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * PackageName : com.timespot.backend.domain.favorite.dao
 * FileName    : FavoriteRepositoryImpl
 * Author      : loadingKKamo21
 * Date        : 26. 3. 24.
 * Description : 즐겨찾기 리포지토리 구현체 (QueryDSL)
 */
@Repository
@RequiredArgsConstructor
public class FavoriteRepositoryImpl implements FavoriteRepositoryCustom {

    private static final QUser     USER     = QUser.user;
    private static final QFavorite FAVORITE = QFavorite.favorite;

    private final JPAQueryFactory queryFactory;

}
