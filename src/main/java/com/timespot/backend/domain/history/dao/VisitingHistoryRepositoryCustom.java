package com.timespot.backend.domain.history.dao;

import com.timespot.backend.domain.history.dto.VisitingHistoryResponseDto.VisitingHistoryListResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * PackageName : com.timespot.backend.domain.history.dao
 * FileName    : VisitingHistoryRepositoryCustom
 * Author      : loadingKKamo21
 * Date        : 26. 3. 24.
 * Description : 방문 이력 리포지토리 커스텀 인터페이스 (QueryDSL)
 */
public interface VisitingHistoryRepositoryCustom {

    Page<VisitingHistoryListResponse> findVisitingHistoryList(UUID userId, String keyword, Pageable pageable);

}
