package com.timespot.backend.domain.history.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.domain.user.model.NotificationTiming;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.http.ResponseEntity;

/**
 * PackageName : com.timespot.backend.domain.history.api
 * FileName    : VisitingHistoryTestApiDocs
 * Author      : loadingKKamo21
 * Date        : 26. 3. 31.
 * Description : 방문 이력 알림 테스트 API 문서 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 31.    loadingKKamo21       Initial creation
 */
@Tag(
        name = "History Test API",
        description = """
                      ## 방문 이력 알림 테스트 API
                      
                      ### 개요
                      푸시 알림 시스템 테스트를 위한 API 입니다.
                      실제 여정을 생성하고, 알림 스케줄링이 정상 작동하는지 확인합니다.
                      
                      ### 주요 기능
                      - **테스트 여정 생성**: 랜덤한 역과 장소를 선택하여 여정을 생성합니다.
                      - **알림 설정 반영**: 사용자의 알림 설정 시간에 따라 푸시 알림이 예약됩니다.
                      
                      ### 인증 방식
                      - **Bearer Token**: JWT 액세스 토큰이 필요합니다.
                      
                      ### 주의사항
                      - **테스트 전용**: 이 API 는 테스트 목적으로만 사용해야 합니다.
                      - **실제 여정 생성**: 생성된 여정은 실제 방문 이력으로 기록됩니다.
                      - **알림 설정 필요**: 사용자의 알림 설정이 되어 있어야 푸시 알림을 받을 수 있습니다.
                      """
)
public interface VisitingHistoryTestApiDocs {

    @Operation(
            summary = "여정 알림 테스트",
            description = """
                          ### 테스트용 여정을 생성하고 알림을 예약합니다.
                          
                          #### 요청 파라미터
                          - `remainingMinutes`: 열차 출발까지 남은 시간 (분)
                          
                          #### 동작 방식
                          1. **랜덤 여정 생성**: 무작위 역과 장소를 선택하여 여정을 생성합니다.
                          2. **알림 예약**: 사용자의 알림 설정 시간에 따라 푸시 알림을 예약합니다.
                          3. **알림 전송**: 예약된 시간에 푸시 알림이 전송됩니다.
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "알림 테스트 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "알림 테스트 성공",
                                    value = """
                                            {
                                              "code": 201,
                                              "message": "새로운 여정이 시작되었습니다.",
                                              "data": {
                                                "historyId": 12345,
                                                "stationName": "서울역",
                                                "placeName": "서울로 7017",
                                                "trainDepartureTime": "2026-03-31T15:00:00",
                                                "remainingMinutes": 5,
                                                "notificationScheduled": true,
                                                "notificationTimings": ["BEFORE_10_MIN", "BEFORE_30_MIN"]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 사용자 또는 역/장소 데이터 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "사용자 없음",
                                            value = """
                                                    {
                                                      "code": 404,
                                                      "message": "사용자를 찾을 수 없습니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "역 없음",
                                            value = """
                                                    {
                                                      "code": 404,
                                                      "message": "역을 찾을 수 없습니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<BaseResponse<TestNotificationResponse>> testJourneyNotification(
            @Parameter(hidden = true)
            @org.springframework.security.core.annotation.AuthenticationPrincipal final com.timespot.backend.common.security.model.CustomUserDetails userDetails,
            @Parameter(
                    description = "열차 출발까지 남은 시간 (분)",
                    required = true,
                    example = "5"
            )
            @org.springframework.web.bind.annotation.RequestParam(value = "remainingMinutes",
                                                                  defaultValue = "60") final int remainingMinutes
    );

    /**
     * 테스트 알림 응답 DTO
     */
    record TestNotificationResponse(
            @Schema(description = "테스트 여정 ID", example = "12345")
            Long historyId,

            @Schema(description = "역 이름", example = "서울역")
            String stationName,

            @Schema(description = "장소 이름", example = "서울로 7017")
            String placeName,

            @Schema(description = "열차 출발 시간", example = "2026-03-31T15:00:00")
            LocalDateTime trainDepartureTime,

            @Schema(description = "남은 시간 (분)", example = "60")
            int remainingMinutes,

            @Schema(description = "알림 예약 여부", example = "true")
            boolean notificationScheduled,

            @Schema(description = "알림 설정 목록", example = "[\"BEFORE_10_MIN\", \"BEFORE_30_MIN\"]")
            Set<NotificationTiming> notificationTimings
    ) {
    }

}
