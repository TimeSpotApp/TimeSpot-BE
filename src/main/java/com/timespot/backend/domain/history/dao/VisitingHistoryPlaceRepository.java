package com.timespot.backend.domain.history.dao;

import com.timespot.backend.domain.history.model.VisitingHistoryPlace;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PackageName : com.timespot.backend.domain.history.dao
 * FileName    : VisitingHistoryPlaceRepository
 * Author      : loadingKKamo21
 * Date        : 26. 3. 25.
 * Description : 방문 이력 장소 리포지토리 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 25.    loadingKKamo21               Initial creation
 */
public interface VisitingHistoryPlaceRepository extends JpaRepository<VisitingHistoryPlace, Long> {
}
