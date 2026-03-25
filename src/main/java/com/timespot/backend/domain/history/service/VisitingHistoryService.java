package com.timespot.backend.domain.history.service;

import com.timespot.backend.domain.history.dto.VisitingHistoryRequestDto.JourneyEndRequest;
import com.timespot.backend.domain.history.dto.VisitingHistoryRequestDto.JourneyStartRequest;
import com.timespot.backend.domain.history.dto.VisitingHistoryResponseDto.VisitingHistoryDetailResponse;
import com.timespot.backend.domain.history.dto.VisitingHistoryResponseDto.VisitingHistoryListResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * PackageName : com.timespot.backend.domain.history.service
 * FileName    : VisitingHistoryService
 * Author      : loadingKKamo21
 * Date        : 26. 3. 25.
 * Description : 방문 이력 서비스 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 25.    loadingKKamo21       Initial creation
 */
public interface VisitingHistoryService {

    VisitingHistoryDetailResponse createNewJourney(UUID userId, JourneyStartRequest dto);

    VisitingHistoryDetailResponse endJourney(UUID userId, Long historyId, JourneyEndRequest dto);

    Page<VisitingHistoryListResponse> getVisitingHistoryList(UUID userId, String keyword, Pageable pageable);

}
