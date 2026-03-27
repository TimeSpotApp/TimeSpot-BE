package com.timespot.backend.domain.device.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.device.dto.DeviceRequestDto.DeviceRegisterRequest;
import com.timespot.backend.domain.device.dto.DeviceResponseDto.DeviceRegisterResponse;
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
 * PackageName : com.timespot.backend.domain.device.api
 * FileName    : DeviceApiDocs
 * Author      : loadingKKamo21
 * Date        : 26. 3. 27.
 * Description : 디바이스 관련 API 문서화 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 27.    loadingKKamo21       Initial creation
 */
@Tag(
        name = "Device API",
        description = """
                      ## 디바이스 관리 API
                      
                      iOS/Android 디바이스 등록 및 삭제 기능을 제공합니다.
                      
                      ### 인증 방식
                      - **디바이스 등록/삭제**: 토큰 불필요 (비회원도 사용 가능)
                      - 요청 헤더에 `Authorization: Bearer {accessToken}` 가 있는 경우 회원과 자동 연동
                      - 없는 경우 비회원 디바이스로 등록
                      
                      ### 주요 기능
                      - **디바이스 등록**: APNs/FCM 토큰을 서버에 등록합니다.
                      - **디바이스 삭제**: 로그아웃/앱 삭제 시 디바이스를 비활성화합니다.
                      
                      ### 회원/비회원 디바이스 처리
                      | 구분 | 처리 방식 |
                      |------|-----------|
                      | **비회원** | `user_id` 없이 디바이스 토큰만 저장 |
                      | **회원** | `user_id` 와 함께 저장, 여러 기기 등록 가능 |
                      | **로그아웃** | 디바이스 비활성화 (`is_active = false`) |
                      | **앱 재설치** | 새 토큰 발급 → 기존 토큰 비활성화, 새 토큰 등록 |
                      | **회원가입** | 비회원 디바이스를 회원과 자동 연동 |
                      
                      ### 플랫폼 타입
                      - `IOS`: iOS (iPhone, iPad) - 현재 지원
                      - `ANDROID`: Android - 향후 확장용
                      
                      ### 알림 설정
                      - 알림 설정은 **회원 전용** 기능입니다.
                      - 로그인 후 **User API**를 이용하세요.
                      - `/api/v1/users/me/notification-settings`
                      """
)
public interface DeviceApiDocs {

    @Operation(
            summary = "디바이스 등록",
            description = """
                          ### iOS/Android 디바이스의 푸시 알림 토큰을 등록합니다.
                          
                          #### 요청 헤더 (선택)
                          - `Authorization: Bearer {accessToken}`
                            - **있는 경우**: 현재 로그인한 사용자와 자동 연동
                            - **없는 경우**: 비회원 디바이스로 등록 (추후 회원가입 시 연동 가능)
                          
                          #### 요청 본문
                          - `deviceToken`: APNs/FCM 토큰 (필수)
                            - iOS: 64 바이트 16 진수 문자열 (32 글자)
                            - Android: FCM 토큰 문자열
                          - `platform`: 플랫폼 유형 (필수)
                            - `IOS`: iOS (iPhone, iPad)
                            - `ANDROID`: Android (향후 확장)
                          - `appVersion`: 앱 버전 (선택)
                            - 예: "1.2.0"
                            - 알림 호환성 버전 체크용
                          - `deviceModel`: 디바이스 모델명 (선택)
                            - 예: "iPhone15,3", "iPad14,5"
                            - 통계 및 분석용으로 사용
                          
                          #### 동작 방식
                          1. **첫 등록**: 새 디바이스 생성
                          2. **기존 토큰 재등록**: `is_active = true` 로 갱신
                          3. **회원 로그인 중**: 현재 사용자와 자동 연동
                          4. **비회원**: `user_id = NULL` 로 저장 (추후 연동 가능)
                          
                          #### 주의사항
                          - 앱 재설치 시 새 토큰이 발급되므로 **재등록 필요**
                          - 한 사용자가 여러 기기 등록 가능 (아이패드, 아이폰 동시 사용)
                          - 토큰은 32 글자 16 진수 문자열 (iOS 기준)
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "디바이스 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DeviceRegisterResponse.class),
                            examples = @ExampleObject(
                                    name = "등록 성공 (회원)",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "디바이스 등록이 완료되었습니다.",
                                              "data": {
                                                "deviceId": 12345,
                                                "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                                "deviceToken": "f5d8c7b6...e4d3c2",
                                                "platform": "IOS",
                                                "isActive": true,
                                                "createdAt": "2024-01-15T10:30:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "디바이스 등록 성공 (비회원)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "등록 성공 (비회원)",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "디바이스 등록이 완료되었습니다.",
                                              "data": {
                                                "deviceId": 12346,
                                                "userId": null,
                                                "deviceToken": "a1b2c3d4...f5e6d7",
                                                "platform": "IOS",
                                                "isActive": true,
                                                "createdAt": "2024-01-15T11:00:00"
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
                                            name = "디바이스 토큰 누락",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "디바이스 토큰은 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "플랫폼 정보 누락",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "플랫폼 정보는 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "잘못된 플랫폼 값",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "지원하지 않는 플랫폼입니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 유효하지 않은 토큰 (헤더에 Authorization 이 있는 경우)",
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
                    responseCode = "409",
                    description = "중복된 디바이스 토큰 (이미 활성화된 토큰)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "중복 토큰",
                                    value = """
                                            {
                                              "code": 409,
                                              "message": "이미 등록된 디바이스 토큰입니다."
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<BaseResponse<DeviceRegisterResponse>> registerDevice(
            @Parameter(
                    description = "디바이스 등록 요청 페이로드",
                    required = true
            ) @Valid DeviceRegisterRequest dto,
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(
            summary = "디바이스 삭제",
            description = """
                          ### 현재 디바이스를 비활성화합니다.
                          
                          #### 요청 헤더 (선택)
                          - `Authorization: Bearer {accessToken}`
                            - **있는 경우**: 회원의 모든 디바이스 비활성화
                            - **없는 경우**: 요청한 디바이스 토큰만 비활성화
                          
                          #### 동작 방식
                          1. **회원**: 현재 사용자의 모든 디바이스를 `is_active = false` 처리
                          2. **비회원**: 요청한 디바이스 토큰을 `is_active = false` 처리
                          3. **하드 삭제 금지**: 통계 및 분석을 위해 소프트 삭제
                          
                          #### 호출 시기
                          - **로그아웃**: 앱에서 로그아웃할 때
                          - **앱 삭제**: 앱 삭제 전 (선택적)
                          - **알림 수신 거부**: 사용자가 알림을 끄고 싶을 때
                          
                          #### 주의사항
                          - 삭제 후 재등록 가능 (새 토큰으로)
                          - 기존 통계 데이터는 유지됩니다.
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "디바이스 삭제 (비활성화) 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "삭제 성공",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "디바이스 등록이 해제되었습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "등록되지 않은 디바이스",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "디바이스 없음",
                                    value = """
                                            {
                                              "code": 404,
                                              "message": "등록되지 않은 디바이스입니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (헤더에 Authorization 이 있는 경우)",
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
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<BaseResponse<Void>> unregisterDevice(
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

}
