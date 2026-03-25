package com.timespot.backend.domain.history.api;

import static com.timespot.backend.common.response.SuccessCode.HISTORY_CREATE_SUCCESS;
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
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.timespot.backend.domain.history.api
 * FileName    : VisitingHistoryController
 * Author      : loadingKKamo21
 * Date        : 26. 3. 26.
 * Description : 방문 이력 API 컨트롤러 (여정 시작, 종료, 목록 조회)
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

    @PostMapping("/{historyId}/end")
    @Override
    public ResponseEntity<BaseResponse<VisitingHistoryDetailResponse>> endJourney(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @PathVariable @Min(1) final Long historyId,
            @RequestBody @Valid final JourneyEndRequest dto
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
            @Parameter(
                    description = "검색어 (역 이름, 역 주소, 장소 이름, 장소 주소, 부분 일치, 대소문자 구분 없음)",
                    example = "서울",
                    required = false
            )
            @RequestParam(required = false, defaultValue = "") final String keyword,
            @Parameter(
                    description = "페이지 번호 (1 부터 시작)",
                    example = "1",
                    required = false
            )
            @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
            @RequestParam(required = false, defaultValue = "1") final int page,
            @Parameter(
                    description = "페이지 크기 (한 페이지당 요소 개수, 최소 10)",
                    example = "10",
                    required = false
            )
            @Min(value = 10, message = "페이지 크기는 최소 10 이상이어야 합니다.")
            @RequestParam(required = false, defaultValue = "10") final int size,
            @Parameter(
                    description = """
                                  정렬 기준 (프로퍼티,방향) - 쉼표로 여러 개 지정 가능
                                  - 프로퍼티: createdAt, duration (소요 시간)
                                  - 방향: ASC, DESC (대소문자 구분 없음)
                                  - 예시: `createdAt,DESC` 또는 `duration,DESC,createdAt,ASC`
                                  """,
                    example = "createdAt,DESC",
                    required = false
            )
            @Pattern(
                    regexp = "^(createdAt|duration),(ASC|DESC|asc|desc)(,\\s*(createdAt|duration),(ASC|DESC|asc|desc)" +
                             ")*$",
                    message = "정렬 형식이 올바르지 않습니다. (예: createdAt,DESC 또는 duration,DESC)"
            )
            @RequestParam(required = false, defaultValue = "createdAt,DESC") final String sort
    ) {
        Pageable pageable = SortUtils.createPageable(page, size, sort);
        Page<VisitingHistoryListResponse> responseData = visitingHistoryService.getVisitingHistoryList(
                userDetails.getId(), keyword, pageable
        );
        return ResponseEntity.ok(BaseResponse.success(HISTORY_GET_SUCCESS, responseData));
    }

}
