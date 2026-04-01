package com.timespot.backend.domain.history.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * PackageName : com.timespot.backend.domain.history.api
 * FileName    : NotificationTestApiDocs
 * Author      : loadingKKamo21
 * Date        : 26. 3. 28.
 * Description : 알림 테스트 API 문서화 인터페이스 (개발용)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 28.    loadingKKamo21       Initial creation
 */
@Tag(
        name = "Notification Test API (개발용)",
        description = """
                      ## 알림 테스트 API (개발/디버깅 전용)
                      
                      ### ⚠️ 주의사항
                      - **개발 환경 전용**: 이 API 는 개발 및 디버깅 목적으로만 사용됩니다.
                      - **운영 환경 비노출**: 운영 환경에서는 제거되거나 비활성화되어야 합니다.
                      - **인증 필요**: 모든 API 는 `Bearer Token` 인증이 필요합니다.
                      - **실제 디바이스 전송**: 이 API 를 호출하면 실제 사용자의 디바이스에 알림이 전송됩니다.
                      
                      ### 🎯 주요 기능
                      | API | 알림 타입 | 메시지 | 스키마 URI |
                      |-----|----------|--------|-----------|
                      | `POST /departure-time` | `DEPARTURE_TIME` | "지금 바로 출발해야 해요!" | `timespot://departure_time` |
                      | `POST /5-min-before` | `BEFORE_5_MINUTES` | "5 분 뒤면 역으로 슬슬 일어날 채비를 할 시간이에요!" | `timespot://5_min_before` |
                      | `POST /10-min-before` | `BEFORE_10_MINUTES` | "10 분 뒤면 역으로 출발해야 해요!" | `timespot://10_min_before` |
                      | `POST /15-min-before` | `BEFORE_15_MINUTES` | "15 분 뒤면 역으로 출발하기까지 15 분 남았어요!" | `timespot://15_min_before` |
                      | `POST /end-journey` | `END_JOURNEY` | "이번 여정은 어떠셨나요? 열차에 잘 탑승하셨나요?" | `timespot://end_journey` |
                      
                      ### 🔔 APNS 알림 구조
                      ```json
                      {
                        "aps": {
                          "alert": {
                            "title": "테스트 알림",
                            "body": "알림 타입별 메시지"
                          },
                          "badge": 1
                        },
                        "customPayload": {
                          "timing": "departure_time",
                          "notificationSchema": "timespot://departure_time",
                          "isTest": true
                        }
                      }
                      ```
                      
                      ### 📱 클라이언트 처리 가이드
                      1. **알림 수신**: APNS 를 통해 알림을 받습니다.
                      2. **스키마 URI 파싱**: `notificationSchema` 필드에서 스키마 URI 를 추출합니다.
                      3. **딥링크 처리**: 스키마 URI 에 따라 적절한 화면으로 이동합니다.
                      4. **테스트 여부 확인**: `isTest: true` 인 경우 테스트용 알림임을 확인합니다.
                      
                      ### 사용 방법
                      1. Swagger UI 에서 테스트할 알림 타입 선택
                      2. `POST /api/v1/test/notifications/{timing}` 요청
                      3. 응답으로 전송된 알림 정보 확인
                      4. 실제 디바이스에서 알림 수신 확인
                      """
)
@SecurityRequirement(name = "BearerAuth")
public interface NotificationTestApiDocs {

    @Operation(
            summary = "[테스트] 즉시 출발 알림 전송",
            description = """
                          ### "지금 바로 출발해야 해요!" 알림을 테스트합니다.
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 필수
                          
                          #### 요청 URL
                          - `POST /api/v1/test/notifications/departure-time`
                          
                          #### 처리 과정
                          1. 사용자의 활성 APNS 토큰 조회
                          2. "지금 바로 출발해야 해요!" 메시지 생성
                          3. 모든 활성 디바이스에 알림 전송
                          4. 전송 결과 반환
                          
                          #### 응답 데이터
                          - `notificationType`: 알림 타입 (`DEPARTURE_TIME`)
                          - `notificationSchema`: 알림 스키마 URI (`timespot://departure_time`)
                          - `sentDeviceCount`: 전송된 디바이스 수
                          - `title`: 알림 제목 ("테스트 알림")
                          - `body`: 알림 본문 ("지금 바로 출발해야 해요!")
                          - `badge`: 뱃지 카운트 (1)
                          - `customPayload`: 커스텀 페이로드 (timing, notificationSchema, isTest)
                          
                          #### 주의사항
                          - 개발 환경에서만 사용하세요.
                          - 실제 사용자의 디바이스에 알림이 전송됩니다.
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 테스트 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = NotificationTestController.NotificationTestResponse.class),
                            examples = @ExampleObject(
                                    name = "전송 성공",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "notificationType": "DEPARTURE_TIME",
                                                "notificationSchema": "timespot://departure_time",
                                                "sentDeviceCount": 2,
                                                "title": "테스트 알림",
                                                "body": "지금 바로 출발해야 해요!",
                                                "badge": 1,
                                                "customPayload": {
                                                  "timing": "departure_time",
                                                  "notificationSchema": "timespot://departure_time",
                                                  "isTest": true
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = """
                                            {
                                              "code": 401,
                                              "message": "인증 정보가 유효하지 않습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "등록된 APNS 토큰 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "APNS 토큰 없음",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "notificationType": "DEPARTURE_TIME",
                                                "notificationSchema": "timespot://departure_time",
                                                "sentDeviceCount": 0,
                                                "title": "테스트 알림",
                                                "body": "지금 바로 출발해야 해요!",
                                                "badge": 1,
                                                "customPayload": {
                                                  "timing": "departure_time",
                                                  "notificationSchema": "timespot://departure_time",
                                                  "isTest": true
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<BaseResponse<NotificationTestController.NotificationTestResponse>> testDepartureTime(
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(
            summary = "[테스트] 5 분 전 알림 전송",
            description = """
                          ### "5 분 뒤면 역으로 슬슬 일어날 채비를 할 시간이에요!" 알림을 테스트합니다.
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 필수
                          
                          #### 요청 URL
                          - `POST /api/v1/test/notifications/5-min-before`
                          
                          #### 응답 데이터
                          - `notificationType`: 알림 타입 (`BEFORE_5_MINUTES`)
                          - `notificationSchema`: 알림 스키마 URI (`timespot://5_min_before`)
                          - `sentDeviceCount`: 전송된 디바이스 수
                          - `title`: 알림 제목 ("테스트 알림")
                          - `body`: 알림 본문 ("5 분 뒤면 역으로 슬슬 일어날 채비를 할 시간이에요!")
                          - `badge`: 뱃지 카운트 (1)
                          - `customPayload`: 커스텀 페이로드 (timing, notificationSchema, isTest)
                          
                          #### 주의사항
                          - 개발 환경에서만 사용하세요.
                          - 실제 사용자의 디바이스에 알림이 전송됩니다.
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 테스트 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = NotificationTestController.NotificationTestResponse.class),
                            examples = @ExampleObject(
                                    name = "전송 성공",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "notificationType": "BEFORE_5_MINUTES",
                                                "notificationSchema": "timespot://5_min_before",
                                                "sentDeviceCount": 2,
                                                "title": "테스트 알림",
                                                "body": "5 분 뒤면 역으로 슬슬 일어날 채비를 할 시간이에요!",
                                                "badge": 1,
                                                "customPayload": {
                                                  "timing": "before_5_minutes",
                                                  "notificationSchema": "timespot://5_min_before",
                                                  "isTest": true
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = """
                                            {
                                              "code": 401,
                                              "message": "인증 정보가 유효하지 않습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<BaseResponse<NotificationTestController.NotificationTestResponse>> test5MinBefore(
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(
            summary = "[테스트] 10 분 전 알림 전송",
            description = """
                          ### "10 분 뒤면 역으로 출발해야 해요!" 알림을 테스트합니다.
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 필수
                          
                          #### 요청 URL
                          - `POST /api/v1/test/notifications/10-min-before`
                          
                          #### 응답 데이터
                          - `notificationType`: 알림 타입 (`BEFORE_10_MINUTES`)
                          - `notificationSchema`: 알림 스키마 URI (`timespot://10_min_before`)
                          - `sentDeviceCount`: 전송된 디바이스 수
                          - `title`: 알림 제목 ("테스트 알림")
                          - `body`: 알림 본문 ("10 분 뒤면 역으로 출발해야 해요!")
                          - `badge`: 뱃지 카운트 (1)
                          - `customPayload`: 커스텀 페이로드 (timing, notificationSchema, isTest)
                          
                          #### 주의사항
                          - 개발 환경에서만 사용하세요.
                          - 실제 사용자의 디바이스에 알림이 전송됩니다.
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 테스트 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = NotificationTestController.NotificationTestResponse.class),
                            examples = @ExampleObject(
                                    name = "전송 성공",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "notificationType": "BEFORE_10_MINUTES",
                                                "notificationSchema": "timespot://10_min_before",
                                                "sentDeviceCount": 2,
                                                "title": "테스트 알림",
                                                "body": "10 분 뒤면 역으로 출발해야 해요!",
                                                "badge": 1,
                                                "customPayload": {
                                                  "timing": "before_10_minutes",
                                                  "notificationSchema": "timespot://10_min_before",
                                                  "isTest": true
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = """
                                            {
                                              "code": 401,
                                              "message": "인증 정보가 유효하지 않습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<BaseResponse<NotificationTestController.NotificationTestResponse>> test10MinBefore(
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(
            summary = "[테스트] 15 분 전 알림 전송",
            description = """
                          ### "15 분 뒤면 역으로 출발하기까지 15 분 남았어요!" 알림을 테스트합니다.
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 필수
                          
                          #### 요청 URL
                          - `POST /api/v1/test/notifications/15-min-before`
                          
                          #### 응답 데이터
                          - `notificationType`: 알림 타입 (`BEFORE_15_MINUTES`)
                          - `notificationSchema`: 알림 스키마 URI (`timespot://15_min_before`)
                          - `sentDeviceCount`: 전송된 디바이스 수
                          - `title`: 알림 제목 ("테스트 알림")
                          - `body`: 알림 본문 ("15 분 뒤면 역으로 출발하기까지 15 분 남았어요!")
                          - `badge`: 뱃지 카운트 (1)
                          - `customPayload`: 커스텀 페이로드 (timing, notificationSchema, isTest)
                          
                          #### 주의사항
                          - 개발 환경에서만 사용하세요.
                          - 실제 사용자의 디바이스에 알림이 전송됩니다.
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 테스트 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = NotificationTestController.NotificationTestResponse.class),
                            examples = @ExampleObject(
                                    name = "전송 성공",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "notificationType": "BEFORE_15_MINUTES",
                                                "notificationSchema": "timespot://15_min_before",
                                                "sentDeviceCount": 2,
                                                "title": "테스트 알림",
                                                "body": "15 분 뒤면 역으로 출발하기까지 15 분 남았어요!",
                                                "badge": 1,
                                                "customPayload": {
                                                  "timing": "before_15_minutes",
                                                  "notificationSchema": "timespot://15_min_before",
                                                  "isTest": true
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = """
                                            {
                                              "code": 401,
                                              "message": "인증 정보가 유효하지 않습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<BaseResponse<NotificationTestController.NotificationTestResponse>> test15MinBefore(
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(
            summary = "[테스트] 여정 종료 알림 전송",
            description = """
                          ### "이번 여정은 어떠셨나요? 열차에 잘 탑승하셨나요?" 알림을 테스트합니다.
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 필수
                          
                          #### 요청 URL
                          - `POST /api/v1/test/notifications/end-journey`
                          
                          #### 응답 데이터
                          - `notificationType`: 알림 타입 (`END_JOURNEY`)
                          - `notificationSchema`: 알림 스키마 URI (`timespot://end_journey`)
                          - `sentDeviceCount`: 전송된 디바이스 수
                          - `title`: 알림 제목 ("테스트 알림")
                          - `body`: 알림 본문 ("이번 여정은 어떠셨나요? 열차에 잘 탑승하셨나요?")
                          - `badge`: 뱃지 카운트 (1)
                          - `customPayload`: 커스텀 페이로드 (timing, notificationSchema, isTest)
                          
                          #### 주의사항
                          - 개발 환경에서만 사용하세요.
                          - 실제 사용자의 디바이스에 알림이 전송됩니다.
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 테스트 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = NotificationTestController.NotificationTestResponse.class),
                            examples = @ExampleObject(
                                    name = "전송 성공",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "notificationType": "END_JOURNEY",
                                                "notificationSchema": "timespot://end_journey",
                                                "sentDeviceCount": 2,
                                                "title": "테스트 알림",
                                                "body": "이번 여정은 어떠셨나요? 열차에 잘 탑승하셨나요?",
                                                "badge": 1,
                                                "customPayload": {
                                                  "timing": "end_journey",
                                                  "notificationSchema": "timespot://end_journey",
                                                  "isTest": true
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = """
                                            {
                                              "code": 401,
                                              "message": "인증 정보가 유효하지 않습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<BaseResponse<NotificationTestController.NotificationTestResponse>> testEndJourney(
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

}
