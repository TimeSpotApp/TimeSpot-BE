package com.timespot.backend.domain.favorite.dao;

import com.timespot.backend.domain.favorite.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PackageName : com.timespot.backend.domain.favorite.dao
 * FileName    : FavoriteRepository
 * Author      : loadingKKamo21
 * Date        : 26. 3. 24.
 * Description : 즐겨찾기 리포지토리 인터페이스
 */
public interface FavoriteRepository extends JpaRepository<Favorite, Long>, FavoriteRepositoryCustom {
}
