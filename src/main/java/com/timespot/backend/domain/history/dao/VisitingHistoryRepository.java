package com.timespot.backend.domain.history.dao;

import com.timespot.backend.domain.history.model.VisitingHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PackageName : com.timespot.backend.domain.history.dao
 * FileName    : VisitingHistoryRepository
 * Author      : loadingKKamo21
 * Date        : 26. 3. 24.
 * Description : 방문 이력 리포지토리 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 24.    loadingKKamo21       Initial creation
 * 26. 4. 2.     loadingKKamo21       findByIdAndIsSuccessFalse 제거, findById 로 통합
 */
public interface VisitingHistoryRepository extends JpaRepository<VisitingHistory, Long>, VisitingHistoryRepositoryCustom {
}
