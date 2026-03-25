package com.timespot.backend.domain.user.dao;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.timespot.backend.domain.user.dto.QUserResponseDto_UserInfoResponse;
import com.timespot.backend.domain.user.dto.UserResponseDto.UserInfoResponse;
import com.timespot.backend.domain.user.model.QSocialConnection;
import com.timespot.backend.domain.user.model.QUser;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * PackageName : com.timespot.backend.domain.user.dao
 * FileName    : UserRepositoryImpl
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description : 사용자 리포지토리 커스텀 구현체 (QueryDSL)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private static final QUser             USER              = QUser.user;
    private static final QSocialConnection SOCIAL_CONNECTION = QSocialConnection.socialConnection;

    private final JPAQueryFactory queryFactory;

    /**
     * ID로 회원 정보 조회
     *
     * @param id 회원 ID
     * @return 회원 정보 응답 DTO
     */
    @Override
    public Optional<UserInfoResponse> findUserInfoById(final UUID id) {
        return Optional.ofNullable(queryFactory.select(new QUserResponseDto_UserInfoResponse(
                                                       USER.id,
                                                       USER.email,
                                                       USER.nickname,
                                                       USER.mapApi,
                                                       USER.role,
                                                       SOCIAL_CONNECTION.providerType,
                                                       USER.totalVisitCount,
                                                       USER.totalJourneyMinutes,
                                                       USER.createdAt
                                               ))
                                               .from(USER)
                                               .join(SOCIAL_CONNECTION).on(USER.id.eq(SOCIAL_CONNECTION.user.id))
                                               .where(USER.id.eq(id))
                                               .fetchOne());
    }

}
