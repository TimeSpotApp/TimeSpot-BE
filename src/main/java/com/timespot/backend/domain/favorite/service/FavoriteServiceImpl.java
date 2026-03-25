package com.timespot.backend.domain.favorite.service;

import static com.timespot.backend.common.response.ErrorCode.FAVORITE_DUPLICATE_STATION;
import static com.timespot.backend.common.response.ErrorCode.FAVORITE_NOT_FOUND;
import static com.timespot.backend.common.response.ErrorCode.STATION_NOT_FOUND;
import static com.timespot.backend.common.response.ErrorCode.USER_NOT_FOUND;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.domain.favorite.dao.FavoriteRepository;
import com.timespot.backend.domain.favorite.dto.FavoriteResponseDto.FavoriteListResponse;
import com.timespot.backend.domain.favorite.model.Favorite;
import com.timespot.backend.domain.place.dao.StationRepository;
import com.timespot.backend.domain.place.model.Station;
import com.timespot.backend.domain.user.dao.UserRepository;
import com.timespot.backend.domain.user.model.User;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PackageName : com.timespot.backend.domain.favorite.service
 * FileName    : FavoriteServiceImpl
 * Author      : loadingKKamo21
 * Date        : 26. 3. 25.
 * Description : 즐겨찾기 도메인 서비스 구현체
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 25.    loadingKKamo21       Initial creation
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository     userRepository;
    private final StationRepository  stationRepository;

    @Override
    @Transactional
    public void createFavoriteStation(final UUID userId, final Long stationId) {
        User    user    = getUserById(userId);
        Station station = getStationById(stationId);

        if (favoriteRepository.existsByUserIdAndStationId(userId, stationId))
            throw new GlobalException(FAVORITE_DUPLICATE_STATION);

        Favorite favorite = Favorite.of(user, station);
        favoriteRepository.save(favorite);
    }

    @Override
    @Transactional
    public void deleteFavoriteStation(final UUID userId, final Long favoriteId) {
        if (!userRepository.existsById(userId)) throw new GlobalException(USER_NOT_FOUND);

        Favorite favorite = favoriteRepository.findById(favoriteId)
                                              .orElseThrow(() -> new GlobalException(FAVORITE_NOT_FOUND));

        if (!Objects.requireNonNull(favorite.getUser().getId()).equals(userId))
            throw new GlobalException(FAVORITE_NOT_FOUND);

        favoriteRepository.delete(favorite);
    }

    @Override
    public Page<FavoriteListResponse> getFavoriteStationList(final UUID userId,
                                                             final String keyword,
                                                             final Pageable pageable) {
        return favoriteRepository.findFavoriteStationList(userId, keyword, pageable);
    }

    // ========================= 내부 메서드 =========================

    /**
     * 사용자 ID 로 사용자 조회
     *
     * @param userId 사용자 ID
     * @return 사용자 엔티티
     * @throws GlobalException 사용자를 찾을 수 없는 경우
     */
    private User getUserById(final UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new GlobalException(USER_NOT_FOUND));
    }

    /**
     * 역 ID 로 역 조회
     *
     * @param stationId 역 ID
     * @return 역 엔티티
     * @throws GlobalException 역을 찾을 수 없는 경우
     */
    private Station getStationById(final Long stationId) {
        return stationRepository.findById(stationId).orElseThrow(() -> new GlobalException(STATION_NOT_FOUND));
    }

}
