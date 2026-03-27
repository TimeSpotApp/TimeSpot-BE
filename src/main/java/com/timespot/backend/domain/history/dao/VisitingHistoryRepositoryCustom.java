package com.timespot.backend.domain.history.dao;

import com.timespot.backend.domain.history.dto.VisitingHistoryResponseDto.VisitingHistoryDetailResponse;
import com.timespot.backend.domain.history.dto.VisitingHistoryResponseDto.VisitingHistoryListResponse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * PackageName : com.timespot.backend.domain.history.dao
 * FileName    : VisitingHistoryRepositoryCustom
 * Author      : loadingKKamo21
 * Date        : 26. 3. 24.
 * Description : 방문 이력 리포지토리 커스텀 인터페이스 (QueryDSL)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 24.    loadingKKamo21       Initial creation
 */
public interface VisitingHistoryRepositoryCustom {

    Optional<VisitingHistoryDetailResponse> findVisitingHistoryDetail(UUID userId, Long historyId);

    /**
     * 사용자의 방문 이력 목록을 조회합니다.
     *
     * @param userId   사용자 ID
     * @param keyword  검색어 (역 이름, 역 주소, 장소 이름, 장소 주소)
     * @param pageable 페이지네이션 정보
     * @return 방문 이력 목록 페이지
     */
    Page<VisitingHistoryListResponse> findVisitingHistoryList(UUID userId, String keyword, Pageable pageable);

}
