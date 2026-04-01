package com.timespot.backend.domain.history.api;

import static com.timespot.backend.common.response.SuccessCode.HISTORY_CREATE_SUCCESS;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.history.service.VisitingHistoryTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.timespot.backend.domain.history.api
 * FileName    : VisitingHistoryTestController
 * Author      : loadingKKamo21
 * Date        : 26. 3. 31.
 * Description : 방문 이력 알림 테스트 API 컨트롤러
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 31.    loadingKKamo21       Initial creation
 */
@RestController
@RequestMapping("/api/v1/histories/test")
@RequiredArgsConstructor
public class VisitingHistoryTestController implements VisitingHistoryTestApiDocs {

    private final VisitingHistoryTestService visitingHistoryTestService;

    @PostMapping
    @Override
    public ResponseEntity<BaseResponse<TestNotificationResponse>> testJourneyNotification(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @RequestParam(value = "remainingMinutes", defaultValue = "30") final int remainingMinutes,
            @RequestParam(value = "walkTimeFromPlace", defaultValue = "10") final int walkTimeFromPlace
    ) {
        TestNotificationResponse responseData = visitingHistoryTestService.testJourneyNotification(
                userDetails.getId(), remainingMinutes, walkTimeFromPlace
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(BaseResponse.success(HISTORY_CREATE_SUCCESS, responseData));
    }

}
