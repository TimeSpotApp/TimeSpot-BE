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
        name = "History Test API (개발용)",
        description = """
                      ## 방문 이력 알림 테스트 API (개발/디버깅 전용)
                      
                      ### ⚠️ 주의사항
                      - **개발 환경 전용**: 이 API 는 개발 및 디버깅 목적으로만 사용됩니다.
                      - **운영 환경 비노출**: 운영 환경에서는 제거되거나 비활성화되어야 합니다.
                      - **실제 여정 생성**: 이 API 는 실제 방문 이력을 생성하며, 삭제되지 않습니다.
                      - **알림 예약**: 사용자의 알림 설정에 따라 실제 푸시 알림이 예약됩니다.
                      
                      ### 🎯 주요 기능
                      - **테스트 여정 생성**: 랜덤한 역과 장소를 선택하여 여정을 생성합니다.
                      - **알림 예약**: 사용자의 알림 설정 시간에 따라 푸시 알림이 예약됩니다.
                      - **walkTimeFromPlace**: 장소에서 역까지 도보 소요 시간을 직접 지정할 수 있습니다.
                      
                      ### 🔔 알림 예약 로직
                      ```
                      1. 여정 시작 (POST /api/v1/histories/test)
                         ↓
                      2. JourneyStartedEvent 발행
                         ↓
                      3. JourneyNotificationScheduler.schedule() 호출
                         ↓
                      4. 알림 시간 계산:
                         - DEPARTURE_TIME: trainDepartureTime - (walkTimeFromPlace + 10 분)
                         - BEFORE_5_MINUTES: 위 시간 - 5 분
                         - BEFORE_10_MINUTES: 위 시간 - 10 분
                         - BEFORE_15_MINUTES: 위 시간 - 15 분
                         - END_JOURNEY: trainDepartureTime
                         ↓
                      5. ScheduledExecutorService 로 예약
                      ```
                      
                      ### 📋 테스트 시나리오
                      1. **즉시 출발 알림 테스트**: `remainingMinutes=30, walkTimeFromPlace=10`
                         - 출발 알림: 30 - (10 + 10) = 10 분 후
                      2. **5 분 전 알림 테스트**: `remainingMinutes=35, walkTimeFromPlace=10`
                         - 5 분 전 알림: 35 - (10 + 10) - 5 = 10 분 후
                      3. **여정 종료 알림 테스트**: `remainingMinutes=60, walkTimeFromPlace=10`
                         - 종료 알림: 60 분 후 (열차 출발 시간)
                      
                      ### 인증 방식
                      - **Bearer Token**: JWT 액세스 토큰이 필요합니다.
                      
                      ### 클라이언트 개발자 가이드
                      1. **테스트용 계정 생성**: 실제 사용자의 계정으로 테스트하지 마세요.
                      2. **알림 설정 확인**: 사용자의 알림 설정 (`/api/v1/users/me/notification-timings`) 을 먼저 확인하세요.
                      3. **파라미터 조합**: `remainingMinutes` 와 `walkTimeFromPlace` 를 조합하여 원하는 알림 시간을 테스트하세요.
                      4. **응답 확인**: `notificationScheduled=true` 이면 알림이 예약된 것입니다.
                      5. **알림 수신**: 예약된 시간에 APNS 알림을 수신하세요.
                      """
)
public interface VisitingHistoryTestApiDocs {

    @Operation(
            summary = "[테스트] 여정 알림 테스트",
            description = """
                          ### 테스트용 여정을 생성하고 알림을 예약합니다.
                          
                          #### ⚠️ 중요
                          - **개발 환경 전용**: 이 API 는 개발 및 디버깅 목적으로만 사용됩니다.
                          - **실제 여정 생성**: 생성된 여정은 실제 방문 이력으로 기록됩니다.
                          - **알림 예약**: 사용자의 알림 설정에 따라 실제 푸시 알림이 예약됩니다.
                          
                          #### 📋 요청 파라미터
                          | 파라미터 | 타입 | 필수 | 기본값 | 설명 |
                          |----------|------|------|--------|------|
                          | `remainingMinutes` | int | ✅ | - | 열차 출발까지 남은 시간 (분) |
                          | `walkTimeFromPlace` | int | ✅ | - | 장소에서 역까지 도보 소요 시간 (분) |
                          
                          #### 🔔 알림 예약 로직
                          ```
                          알림 기준 시간 = trainDepartureTime - (walkTimeFromPlace + 플랫폼 대기 시간 10 분)
                          
                          알림 타입별 발송 시간:
                          - DEPARTURE_TIME: 알림 기준 시간
                          - BEFORE_5_MINUTES: 알림 기준 시간 - 5 분
                          - BEFORE_10_MINUTES: 알림 기준 시간 - 10 분
                          - BEFORE_15_MINUTES: 알림 기준 시간 - 15 분
                          - END_JOURNEY: trainDepartureTime (열차 출발 시간)
                          ```
                          
                          #### 📊 테스트 시나리오 예시
                          | 시나리오 | remainingMinutes | walkTimeFromPlace | 출발 알림 시간 |
                          |---------|-------------------|-------------------|---------------|
                          | 즉시 출발 | 30 | 10 | 10 분 후 |
                          | 5 분 전 알림 | 35 | 10 | 10 분 후 |
                          | 10 분 전 알림 | 40 | 10 | 10 분 후 |
                          | 여정 종료 | 60 | 10 | 60 분 후 |
                          
                          #### 응답 데이터
                          - `historyId`: 생성된 테스트 여정 ID
                          - `stationName`: 랜덤 선택된 역 이름
                          - `placeName`: 랜덤 선택된 장소 이름
                          - `trainDepartureTime`: 열차 출발 시간
                          - `remainingMinutes`: 요청한 남은 시간 (분)
                          - `walkTimeFromPlace`: 요청한 도보 시간 (분)
                          - `notificationScheduled`: 알림 예약 여부
                          - `notificationTimings`: 사용자의 알림 설정 목록
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "알림 테스트 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TestNotificationResponse.class),
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
                                                "remainingMinutes": 30,
                                                "walkTimeFromPlace": 10,
                                                "notificationScheduled": true,
                                                "notificationTimings": ["BEFORE_10_MINUTES", "BEFORE_15_MINUTES"]
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
                                                      "message": "해당 회원을 찾을 수 없습니다."
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
                    description = "[필수] 열차 출발까지 남은 시간 (분)",
                    required = true,
                    example = "30"
            )
            @org.springframework.web.bind.annotation.RequestParam(value = "remainingMinutes",
                                                                  defaultValue = "30") final int remainingMinutes,
            @Parameter(
                    description = "[필수] 장소에서 역까지 도보 소요 시간 (분) - 알림 예약에 사용됨",
                    required = true,
                    example = "10"
            )
            @org.springframework.web.bind.annotation.RequestParam(value = "walkTimeFromPlace",
                                                                  defaultValue = "10") final int walkTimeFromPlace
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

            @Schema(description = "장소→역 도보 시간 (분)", example = "10")
            int walkTimeFromPlace,

            @Schema(description = "알림 예약 여부", example = "true")
            boolean notificationScheduled,

            @Schema(description = "알림 설정 목록", example = "[\"BEFORE_10_MIN\", \"BEFORE_30_MIN\"]")
            Set<NotificationTiming> notificationTimings
    ) {
    }

}
