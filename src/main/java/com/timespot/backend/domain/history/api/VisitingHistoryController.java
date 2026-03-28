package com.timespot.backend.domain.history.api;

import static com.timespot.backend.common.response.SuccessCode.HISTORY_CREATE_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.HISTORY_DELETE_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.HISTORY_END_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.HISTORY_GET_SUCCESS;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.annotation.CustomPageResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.common.util.SortUtils;
import com.timespot.backend.domain.history.dto.VisitingHistoryRequestDto.JourneyEndRequest;
import com.timespot.backend.domain.history.dto.VisitingHistoryRequestDto.JourneyStartRequest;
import com.timespot.backend.domain.history.dto.VisitingHistoryResponseDto.VisitingHistoryDetailResponse;
import com.timespot.backend.domain.history.dto.VisitingHistoryResponseDto.VisitingHistoryListResponse;
import com.timespot.backend.domain.history.service.VisitingHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.timespot.backend.domain.history.api
 * FileName    : VisitingHistoryController
 * Author      : loadingKKamo21
 * Date        : 26. 3. 26.
 * Description : 방문 이력 API 컨트롤러 (여정 시작, 종료, 목록 조회, 삭제)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 26.    loadingKKamo21       Initial creation
 */
@RestController
@RequestMapping("/api/v1/histories")
@RequiredArgsConstructor
public class VisitingHistoryController implements VisitingHistoryApiDocs {

    private final VisitingHistoryService visitingHistoryService;

    @PostMapping
    @Override
    public ResponseEntity<BaseResponse<VisitingHistoryDetailResponse>> createNewJourney(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @RequestBody @Valid final JourneyStartRequest dto
    ) {
        VisitingHistoryDetailResponse responseData = visitingHistoryService.createNewJourney(
                userDetails.getId(), dto
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(BaseResponse.success(HISTORY_CREATE_SUCCESS, responseData));
    }

    @PutMapping("/{historyId}")
    @Override
    public ResponseEntity<BaseResponse<VisitingHistoryDetailResponse>> endJourney(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @PathVariable final Long historyId,
            @RequestBody final JourneyEndRequest dto
    ) {
        VisitingHistoryDetailResponse responseData = visitingHistoryService.endJourney(
                userDetails.getId(), historyId, dto
        );
        return ResponseEntity.ok(BaseResponse.success(HISTORY_END_SUCCESS, responseData));
    }

    @GetMapping
    @CustomPageResponse(
            numberOfElements = false,
            empty = false,
            hasContent = false,
            first = false,
            last = false,
            hasPrevious = false
    )
    @Override
    public ResponseEntity<BaseResponse<Page<VisitingHistoryListResponse>>> getVisitingHistoryList(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @RequestParam(required = false, defaultValue = "") final String keyword,
            @RequestParam(required = false, defaultValue = "1") final int page,
            @RequestParam(required = false, defaultValue = "10") final int size,
            @RequestParam(required = false, defaultValue = "createdAt,DESC") final String sort
    ) {
        Pageable pageable = SortUtils.createPageable(page, size, sort);
        Page<VisitingHistoryListResponse> responseData = visitingHistoryService.getVisitingHistoryList(
                userDetails.getId(), keyword, pageable
        );
        return ResponseEntity.ok(BaseResponse.success(HISTORY_GET_SUCCESS, responseData));
    }

    @DeleteMapping("/{historyId}")
    @Override
    public ResponseEntity<BaseResponse<Void>> deleteJourney(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @PathVariable final Long historyId
    ) {
        visitingHistoryService.deleteJourney(userDetails.getId(), historyId);
        return ResponseEntity.ok(BaseResponse.success(HISTORY_DELETE_SUCCESS));
    }

}
