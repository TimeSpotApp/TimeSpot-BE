package com.timespot.backend.domain.user.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.security.dto.AuthResponseDto.AuthInfoResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.user.dto.UserNotificationRequestDto.NotificationSettingsRequest;
import com.timespot.backend.domain.user.dto.UserNotificationResponseDto.NotificationSettingsResponse;
import com.timespot.backend.domain.user.dto.UserRequestDto.UserInfoUpdateRequest;
import com.timespot.backend.domain.user.dto.UserResponseDto.UserInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

/**
 * PackageName : com.timespot.backend.domain.user.api
 * FileName    : UserApiDocs
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description : 사용자 관련 API 문서화 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 * 26. 3. 24.    loadingKKamo21       API 문서 상세화 (응답 예시, 에러 코드 추가)
 */
@Tag(
        name = "User API",
        description = """
                      ## 회원 관리 API
                      
                      회원 정보 조회, 수정, 탈퇴 및 알림 설정 기능을 제공합니다.
                      
                      ### 인증 방식
                      - 모든 API 는 `Bearer Token` 인증이 필요합니다.
                      - 요청 헤더에 `Authorization: Bearer {accessToken}` 를 포함해야 합니다.
                      
                      ### 주요 기능
                      - **회원 정보 조회**: 현재 로그인한 사용자의 정보를 조회합니다.
                      - **회원 정보 수정**: 주사용 지도 API 를 변경합니다.
                      - **회원 탈퇴**: 사용자를 탈퇴 처리하고 모든 연동 정보를 삭제합니다.
                      - **알림 설정 조회/수정**: 열차 출발 알림 수신 설정을 관리합니다.
                      
                      ### 알림 설정 (회원 전용)
                      - **회원 전용 기능**: 로그인한 사용자만 사용 가능
                      - **기기 동기화**: 한 기기에서 설정하면 모든 기기에서 적용됨
                      - **비회원**: 디바이스 등록만 가능 (알림 설정 불가)
                      
                      ### 알림 타입
                      | 타입 | 설명 | 수정 가능 |
                      |------|------|-----------|
                      | `DEPARTURE_TIME` | 열차 출발 시간 (기본) | ❌ |
                      | `DEPARTURE_5_MIN_BEFORE` | 출발 5 분 전 | ✅ |
                      | `DEPARTURE_10_MIN_BEFORE` | 출발 10 분 전 | ✅ |
                      | `DEPARTURE_15_MIN_BEFORE` | 출발 15 분 전 | ✅ |
                      """
)
@SecurityRequirement(name = "BearerAuth")
public interface UserApiDocs {

    @Operation(
            summary = "회원 정보 조회",
            description = """
                          ### 현재 사용자의 회원 정보를 조회합니다.
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 필수
                          
                          #### 응답 데이터
                          - `userId`: 회원 UUID (문자열 형식)
                          - `email`: 이메일 주소
                          - `nickname`: 닉네임
                          - `mapApi`: 주사용 지도 API 유형 (APPLE, GOOGLE, NAVER)
                          - `role`: 계정 역할 (USER, ADMIN)
                          - `providerType`: 소셜 인증 제공자 (APPLE, GOOGLE)
                          - `totalVisitCount`: 총 방문 횟수 (여정 횟수)
                          - `totalJourneyMinutes`: 총 여정 누적 시간 (분)
                          - `createdAt`: 가입 일시 (ISO-8601 형식)
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserInfoResponse.class),
                            examples = @ExampleObject(
                                    name = "조회 성공",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "회원 정보 조회이 완료되었습니다.",
                                              "data": {
                                                "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                                "email": "user@example.com",
                                                "nickname": "홍길동",
                                                "mapApi": "GOOGLE",
                                                "role": "USER",
                                                "providerType": "APPLE",
                                                "totalVisitCount": 15,
                                                "totalJourneyMinutes": 450,
                                                "createdAt": "2024-01-15T10:30:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 유효하지 않은 토큰",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "유효하지 않은 토큰",
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
                    description = "회원을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "회원 없음",
                                    value = """
                                            {
                                              "code": 404,
                                              "message": "해당 회원을 찾을 수 없습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<BaseResponse<UserInfoResponse>> getUserInfo(
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(
            summary = "회원 정보 수정",
            description = """
                          ### 현재 사용자의 회원 정보를 수정합니다.
                          
                          #### 수정 가능 항목
                          - **mapApi**: 주사용 지도 API 유형 (apple, google, naver)
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 필수
                          
                          #### 요청 본문
                          - `mapApi`: 수정할 지도 API 유형 (필수, 소문자)
                          
                          #### 응답 데이터
                          - 수정 후 재발급된 토큰 정보가 포함됩니다.
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원 정보 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthInfoResponse.class),
                            examples = @ExampleObject(
                                    name = "수정 성공",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "회원정보 수정이 완료되었습니다.",
                                              "data": {
                                                "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
                                                "accessTokenExpiresIn": 604800,
                                                "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
                                                "refreshTokenExpiresIn": 1209600,
                                                "map": {
                                                  "mapName": "구글",
                                                  "mapUrlScheme": "comgooglemaps"
                                                },
                                                "socialType": "APPLE",
                                                "newUser": false,
                                                "userInfo": {
                                                  "email": "user@example.com",
                                                  "nickname": "닉네임"
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 유효성 검사 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "지도 API 유형 오류",
                                    value = """
                                            {
                                              "code": 400,
                                              "message": "주사용 지도 API 유형은 필수입니다."
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
                    description = "회원을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "회원 없음",
                                    value = """
                                            {
                                              "code": 404,
                                              "message": "해당 회원을 찾을 수 없습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<BaseResponse<AuthInfoResponse>> updateUserInfo(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(
                    description = "회원 정보 수정 요청 페이로드",
                    required = true
            ) @Valid UserInfoUpdateRequest dto
    );

    @Operation(
            summary = "회원 탈퇴",
            description = """
                          ### 현재 사용자를 회원 탈퇴 처리합니다.
                          
                          #### 탈퇴 시 수행되는 작업
                          1. 소셜 연동 정보 삭제 (SocialConnection)
                          2. 사용자 정보 삭제 (User)
                          3. IDP(Apple/Google) 리프레시 토큰 폐기
                          4. Redis 에 저장된 RefreshToken 삭제
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 필수
                          
                          #### 주의사항
                          - 탈퇴 후 복구가 불가능합니다.
                          - 모든 방문 이력, 즐겨찾기 정보가 삭제됩니다.
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원 탈퇴 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "탈퇴 성공",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "회원탈퇴가 완료되었습니다."
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
                    description = "회원 또는 소셜 연동 정보를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "회원 없음",
                                            value = """
                                                    {
                                                      "code": 404,
                                                      "message": "해당 회원을 찾을 수 없습니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "소셜 연동 정보 없음",
                                            value = """
                                                    {
                                                      "code": 404,
                                                      "message": "소셜 연동 정보를 찾을 수 없습니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "IDP 토큰 폐기 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "IDP 토큰 폐기 실패",
                                    value = """
                                            {
                                              "code": 500,
                                              "message": "IDP 인증 토큰 폐기에 실패했습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<BaseResponse<Void>> withdraw(
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(
            summary = "알림 설정 조회",
            description = """
                          ### 현재 사용자의 알림 수신 설정 상태를 조회합니다.
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 필수
                          
                          #### 응답 데이터
                          - **4 가지 알림 타입 모두 반환**
                          - `DEPARTURE_TIME`: 항상 `isEnabled = true`, `isEditable = false`
                          - 나머지 3 가지: 사용자 설정에 따라 `isEnabled` 변동, `isEditable = true`
                          
                          #### 알림 타입별 설명
                          | 타입 | 설명 | 기본값 | 수정 가능 |
                          |------|------|--------|-----------|
                          | `DEPARTURE_TIME` | 열차 출발 시간 | 항상 활성화 | ❌ |
                          | `DEPARTURE_5_MIN_BEFORE` | 출발 5 분 전 | 활성화 | ✅ |
                          | `DEPARTURE_10_MIN_BEFORE` | 출발 10 분 전 | 활성화 | ✅ |
                          | `DEPARTURE_15_MIN_BEFORE` | 출발 15 분 전 | 활성화 | ✅ |
                          
                          #### "설정 안 함" 상태
                          - 3 가지 선택적 알림 모두 `isEnabled = false` 인 상태
                          - `DEPARTURE_TIME` 만 수신
                          
                          #### 기기 동기화
                          - **회원 전용**: 한 기기에서 설정하면 모든 기기에서 적용됨
                          - 예: iPhone 에서 설정 → iPad 에서도 동일하게 적용
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 설정 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationSettingsResponse.class),
                            examples = @ExampleObject(
                                    name = "조회 성공 (모두 활성화)",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "알림 설정 조회가 완료되었습니다.",
                                              "data": {
                                                "settings": [
                                                  {
                                                    "type": "DEPARTURE_TIME",
                                                    "isEnabled": true,
                                                    "isEditable": false
                                                  },
                                                  {
                                                    "type": "DEPARTURE_5_MIN_BEFORE",
                                                    "isEnabled": true,
                                                    "isEditable": true
                                                  },
                                                  {
                                                    "type": "DEPARTURE_10_MIN_BEFORE",
                                                    "isEnabled": true,
                                                    "isEditable": true
                                                  },
                                                  {
                                                    "type": "DEPARTURE_15_MIN_BEFORE",
                                                    "isEnabled": true,
                                                    "isEditable": true
                                                  }
                                                ],
                                                "updatedAt": "2024-01-15T10:30:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 설정 조회 성공 (일부 비활성화)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "조회 성공 (5 분 전만 활성화)",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "알림 설정 조회가 완료되었습니다.",
                                              "data": {
                                                "settings": [
                                                  {
                                                    "type": "DEPARTURE_TIME",
                                                    "isEnabled": true,
                                                    "isEditable": false
                                                  },
                                                  {
                                                    "type": "DEPARTURE_5_MIN_BEFORE",
                                                    "isEnabled": true,
                                                    "isEditable": true
                                                  },
                                                  {
                                                    "type": "DEPARTURE_10_MIN_BEFORE",
                                                    "isEnabled": false,
                                                    "isEditable": true
                                                  },
                                                  {
                                                    "type": "DEPARTURE_15_MIN_BEFORE",
                                                    "isEnabled": false,
                                                    "isEditable": true
                                                  }
                                                ],
                                                "updatedAt": "2024-01-15T10:30:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 설정 조회 성공 (설정 안 함)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "조회 성공 (설정 안 함)",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "알림 설정 조회가 완료되었습니다.",
                                              "data": {
                                                "settings": [
                                                  {
                                                    "type": "DEPARTURE_TIME",
                                                    "isEnabled": true,
                                                    "isEditable": false
                                                  },
                                                  {
                                                    "type": "DEPARTURE_5_MIN_BEFORE",
                                                    "isEnabled": false,
                                                    "isEditable": true
                                                  },
                                                  {
                                                    "type": "DEPARTURE_10_MIN_BEFORE",
                                                    "isEnabled": false,
                                                    "isEditable": true
                                                  },
                                                  {
                                                    "type": "DEPARTURE_15_MIN_BEFORE",
                                                    "isEnabled": false,
                                                    "isEditable": true
                                                  }
                                                ],
                                                "updatedAt": "2024-01-15T10:30:00"
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
                    description = "회원을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "회원 없음",
                                    value = """
                                            {
                                              "code": 404,
                                              "message": "해당 회원을 찾을 수 없습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<BaseResponse<NotificationSettingsResponse>> getNotificationSettings(
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(
            summary = "알림 설정 수정",
            description = """
                          ### 사용자의 알림 수신 설정을 변경합니다.
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 필수
                          
                          #### 요청 본문
                          - `notificationSettings`: 알림 설정 목록 (1~3 개)
                            - `type`: 알림 타입 (`DEPARTURE_5_MIN_BEFORE`, `DEPARTURE_10_MIN_BEFORE`, `DEPARTURE_15_MIN_BEFORE`)
                            - `isEnabled`: 활성화 여부 (`true`/`false`)
                          
                          #### 제약사항
                          - `DEPARTURE_TIME` 은 기본 알림이므로 **제외** (항상 활성화)
                          - 최대 3 개 항목까지 전송 가능
                          - 중복된 타입 전송 불가
                          - 존재하지 않는 타입 전송 불가
                          
                          #### "설정 안 함" 구현 방법
                          ```json
                          {
                            "notificationSettings": [
                              {"type": "DEPARTURE_5_MIN_BEFORE", "isEnabled": false},
                              {"type": "DEPARTURE_10_MIN_BEFORE", "isEnabled": false},
                              {"type": "DEPARTURE_15_MIN_BEFORE", "isEnabled": false}
                            ]
                          }
                          ```
                          
                          #### "모두 활성화" 구현 방법
                          ```json
                          {
                            "notificationSettings": [
                              {"type": "DEPARTURE_5_MIN_BEFORE", "isEnabled": true},
                              {"type": "DEPARTURE_10_MIN_BEFORE", "isEnabled": true},
                              {"type": "DEPARTURE_15_MIN_BEFORE", "isEnabled": true}
                            ]
                          }
                          ```
                          
                          #### 부분 활성화 예시 (5 분 전 + 10 분 전)
                          ```json
                          {
                            "notificationSettings": [
                              {"type": "DEPARTURE_5_MIN_BEFORE", "isEnabled": true},
                              {"type": "DEPARTURE_10_MIN_BEFORE", "isEnabled": true},
                              {"type": "DEPARTURE_15_MIN_BEFORE", "isEnabled": false}
                            ]
                          }
                          ```
                          
                          #### 기기 동기화
                          - **회원 전용**: 한 기기에서 설정하면 모든 기기에서 적용됨
                          - 예: iPhone 에서 수정 → iPad 에서도 자동으로 적용
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 설정 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationSettingsResponse.class),
                            examples = @ExampleObject(
                                    name = "수정 성공 (5 분 전 + 10 분 전 활성화)",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "알림 설정이 변경되었습니다.",
                                              "data": {
                                                "settings": [
                                                  {
                                                    "type": "DEPARTURE_TIME",
                                                    "isEnabled": true,
                                                    "isEditable": false
                                                  },
                                                  {
                                                    "type": "DEPARTURE_5_MIN_BEFORE",
                                                    "isEnabled": true,
                                                    "isEditable": true
                                                  },
                                                  {
                                                    "type": "DEPARTURE_10_MIN_BEFORE",
                                                    "isEnabled": true,
                                                    "isEditable": true
                                                  },
                                                  {
                                                    "type": "DEPARTURE_15_MIN_BEFORE",
                                                    "isEnabled": false,
                                                    "isEditable": true
                                                  }
                                                ],
                                                "updatedAt": "2024-01-15T11:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 유효성 검사 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "설정 목록 누락",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "알림 설정 목록은 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "알림 타입 누락",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "알림 타입은 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "잘못된 알림 타입",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "지원하지 않는 알림 타입입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "활성화 여부 누락",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "활성화 여부는 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "중복된 타입",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "중복된 알림 타입이 포함되어 있습니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "범위 초과 (4 개 이상)",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "알림 설정은 최대 3 개까지 가능합니다."
                                                    }
                                                    """
                                    )
                            }
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
                    description = "회원을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "회원 없음",
                                    value = """
                                            {
                                              "code": 404,
                                              "message": "해당 회원을 찾을 수 없습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<BaseResponse<NotificationSettingsResponse>> updateNotificationSettings(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(
                    description = "알림 설정 수정 요청 페이로드",
                    required = true
            ) @Valid NotificationSettingsRequest dto
    );

}
