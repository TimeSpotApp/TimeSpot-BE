package com.timespot.backend.common.security.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.security.dto.AuthRequestDto.OAuth2LoginRequest;
import com.timespot.backend.common.security.dto.AuthRequestDto.OAuth2SignupRequest;
import com.timespot.backend.common.security.dto.AuthRequestDto.TokenRefreshRequest;
import com.timespot.backend.common.security.dto.AuthResponseDto.AuthInfoResponse;
import com.timespot.backend.common.security.dto.AuthResponseDto.TokenInfoResponse;
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
 * PackageName : com.timespot.backend.common.security.api
 * FileName    : AuthApiDocs
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description : 인증/인가 관련 API 문서화 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 * 26. 3. 24.    loadingKKamo21       API 문서 상세화 (응답 예시, 에러 코드 추가)
 * 26. 3. 28.    loadingKKamo21       디바이스 등록 API 추가 (/api/v1/auth/devices)
 */
@Tag(
        name = "Authentication API",
        description = """
                      ## 인증/인가 API
                      
                      OAuth2 소셜 로그인, 회원가입, 토큰 관리 기능을 제공합니다.
                      
                      ### 인증 흐름
                      1. **회원가입**: Apple/Google 인증 코드로 회원가입
                      2. **로그인**: Apple/Google ID 토큰으로 로그인
                      3. **토큰 갱신**: RefreshToken 으로 AccessToken 재발급
                      4. **로그아웃**: AccessToken 블랙리스트 처리
                      
                      ### 토큰 정보
                      - **AccessToken**: 7 일 유효, API 요청 시 매번 필요
                      - **RefreshToken**: 14 일 유효, 토큰 갱신 시 사용
                      
                      ### 디바이스 관리
                      - **디바이스 등록**: 앱 설치 시 푸시 알림 토큰 등록
                      - **자동 연동**: 로그인 시 회원과 디바이스 자동 연동
                      """
)
public interface AuthApiDocs {

    @Operation(
            summary = "OAuth2 신규 회원 가입",
            description = """
                          ### 소셜 인증 제공자 정보를 사용하여 신규 회원 가입을 진행합니다.
                          
                          #### 회원가입 절차
                          1. Apple/Google OAuth 에서 인증 코드 획득
                          2. 인증 코드를 서버로 전송하여 회원가입
                          3. AccessToken, RefreshToken 발급
                          
                          #### 요청 본문
                          - `provider`: 소셜 인증 제공자 (apple, google) - 필수
                          - `authCode`: 소셜 인증 제공자로부터 발급받은 인증 코드 - 필수
                          - `email`: 사용자 이메일 - 필수
                          - `nickname`: 닉네임 (2~15 자, 한글/영문/숫자/-/_ 허용) - 필수
                          - `mapApi`: 주사용 지도 API (apple, google, naver) - 필수
                          
                          #### 응답 데이터
                          - 발급된 AccessToken, RefreshToken 및 만료 시간
                          - 사용자 정보 (이메일, 닉네임)
                          - 소셜 인증 제공자 유형
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "신규 회원 가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthInfoResponse.class),
                            examples = @ExampleObject(
                                    name = "가입 성공",
                                    value = """
                                            {
                                              "code": 201,
                                              "message": "회원가입이 완료되었습니다.",
                                              "data": {
                                                "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
                                                "accessTokenExpiresIn": 604800,
                                                "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
                                                "refreshTokenExpiresIn": 1209600,
                                                "map": {
                                                  "mapName": "애플",
                                                  "mapUrlScheme": null
                                                },
                                                "socialType": "APPLE",
                                                "newUser": false,
                                                "userInfo": {
                                                  "email": "user@example.com",
                                                  "nickname": "홍길동"
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
                            examples = {
                                    @ExampleObject(
                                            name = "이메일 중복",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "이미 사용 중인 이메일 주소입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "닉네임 형식 오류",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "닉네임은 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "지원하지 않는 제공자",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "소셜 인증 제공자 유형이 올바르지 않습니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "IDP 인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "IDP 인증 실패",
                                    value = """
                                            {
                                              "code": 500,
                                              "message": "IDP 인증에 실패했습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<BaseResponse<AuthInfoResponse>> signup(
            @Parameter(
                    description = "소셜 인증 제공자로부터 발급받은 인증 코드 페이로드",
                    required = true
            ) @Valid OAuth2SignupRequest dto
    );

    @Operation(
            summary = "OAuth2 소셜 로그인",
            description = """
                          ### 소셜 인증 제공자의 토큰을 받아 자체 서비스 액세스 토큰을 발급합니다.
                          
                          #### 로그인 절차
                          1. Apple/Google OAuth 에서 ID 토큰 획득
                          2. ID 토큰을 서버로 전송하여 로그인
                          3. 기존 사용자: AccessToken, RefreshToken 발급
                          4. 신규 사용자: 회원가입 필요 안내 (newUser: true)
                          
                          #### 요청 본문
                          - `provider`: 소셜 인증 제공자 (apple, google) - 필수
                          - `idToken`: 소셜 인증 제공자로부터 발급받은 ID 토큰 - 필수
                          
                          #### 응답 데이터
                          - **기존 사용자**: AccessToken, RefreshToken 및 사용자 정보
                          - **신규 사용자**: newUser=true, 소셜 인증 제공자 정보만 반환 (토큰 없음)
                          
                          #### HTTP 상태 코드
                          - `200 OK`: 기존 사용자 로그인 성공
                          - `202 Accepted`: 신규 사용자 (회원가입 필요)
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공 (기존 사용자)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthInfoResponse.class),
                            examples = @ExampleObject(
                                    name = "로그인 성공 (기존 사용자)",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "로그인 되었습니다.",
                                              "data": {
                                                "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
                                                "accessTokenExpiresIn": 604800,
                                                "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
                                                "refreshTokenExpiresIn": 1209600,
                                                "map": {
                                                  "mapName": "구글",
                                                  "mapUrlScheme": "comgooglemaps"
                                                },
                                                "socialType": "GOOGLE",
                                                "newUser": false,
                                                "userInfo": {
                                                  "email": "user@example.com",
                                                  "nickname": "홍길동"
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "202",
                    description = "신규 사용자 감지 (회원가입 필요)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "신규 사용자",
                                    value = """
                                            {
                                              "code": 202,
                                              "message": "로그인 요청 처리가 완료되었습니다.",
                                              "data": {
                                                "socialType": "APPLE",
                                                "newUser": true,
                                                "userInfo": {
                                                  "email": "newuser@example.com",
                                                  "nickname": null
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
                            examples = {
                                    @ExampleObject(
                                            name = "유효하지 않은 토큰",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "잘못되거나 만료된 소셜 인증 토큰입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "지원하지 않는 제공자",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "소셜 인증 제공자 유형이 올바르지 않습니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "IDP 인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "IDP 인증 실패",
                                    value = """
                                            {
                                              "code": 500,
                                              "message": "IDP 인증에 실패했습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<BaseResponse<AuthInfoResponse>> login(
            @Parameter(
                    description = "소셜 인증 제공자로부터 발급받은 ID 토큰 페이로드",
                    required = true
            ) @Valid OAuth2LoginRequest dto
    );

    @Operation(
            summary = "로그아웃",
            description = """
                          ### 현재 사용자의 액세스 토큰을 만료 처리하고 로그아웃합니다.
                          
                          #### 로그아웃 처리
                          1. Redis 에서 RefreshToken 삭제
                          2. AccessToken 을 블랙리스트에 등록 (잔여 유효시간 동안)
                          3. 블랙리스트 기간 동안 해당 AccessToken 사용 불가
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 필수
                          """
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "로그아웃 성공",
                            value = """
                                    {
                                      "code": 200,
                                      "message": "로그아웃 되었습니다."
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "인증 헤더 없음",
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
    ResponseEntity<BaseResponse<Void>> logout(@Parameter(hidden = true) String authorizationHeader);

    @Operation(
            summary = "토큰 갱신",
            description = """
                          ### 리프레시 토큰을 사용하여 새로운 토큰을 발급받습니다.
                          
                          #### 토큰 갱신 절차
                          1. RefreshToken 유효성 검증
                          2. Redis 에 저장된 토큰과 일치 여부 확인
                          3. 새로운 AccessToken, RefreshToken 발급
                          4. 기존 RefreshToken 을 Grace Period(30 초) 로 설정
                          5. 새 RefreshToken 을 Redis 에 저장
                          
                          #### Grace Period
                          - 기존 RefreshToken 으로 요청 시 30 초간 새 RefreshToken 반환
                          - 동시 요청 방지를 위한 안전 장치
                          
                          #### 요청 본문
                          - `refreshToken`: JWT Refresh Token - 필수
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenInfoResponse.class),
                            examples = @ExampleObject(
                                    name = "토큰 갱신 성공",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "인증 토큰 갱신이 완료되었습니다.",
                                              "data": {
                                                "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
                                                "accessTokenExpiresIn": 604800,
                                                "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
                                                "refreshTokenExpiresIn": 1209600
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "RefreshToken 유효성 검사 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "만료된 RefreshToken",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "RefreshToken 이 만료되었습니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "잘못된 RefreshToken",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "잘못된 RefreshToken 입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "RefreshToken 불일치",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "잘못된 RefreshToken 입니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<BaseResponse<TokenInfoResponse>> refresh(
            @Parameter(
                    description = "리프레시 토큰 페이로드",
                    required = true
            ) @Valid TokenRefreshRequest dto
    );

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
                            - iOS: 64 바이트 16 진수 문자열
                            - Android: FCM 토큰 문자열
                            - 앱 재설치 시마다 재발급됨
                          
                          #### 동작 방식
                          1. **첫 등록**: 새 디바이스 생성
                          2. **기존 토큰 재등록**: `is_active = true` 로 갱신
                          3. **회원 로그인 중**: 현재 사용자와 자동 연동
                          4. **비회원**: `user_id = NULL` 로 저장 (추후 연동 가능)
                          
                          #### 주의사항
                          - 앱 재설치 시 새 토큰이 발급되므로 **재등록 필요**
                          - 한 사용자가 여러 기기 등록 가능 (아이패드, 아이폰 동시 사용)
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
                                                "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                                "deviceToken": "f5d8c7b6...e4d3c2",
                                                "isActive": true
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
                                                "userId": null,
                                                "deviceToken": "a1b2c3d4...f5e6d7",
                                                "isActive": true
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
                                    name = "디바이스 토큰 누락",
                                    value = """
                                            {
                                              "code": 400,
                                              "message": "디바이스 토큰은 필수입니다."
                                            }
                                            """
                            )
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
            )
    })
    ResponseEntity<BaseResponse<DeviceRegisterResponse>> registerDevice(
            @Parameter(
                    description = "디바이스 등록 요청 페이로드",
                    required = true
            ) @Valid DeviceRegisterRequest dto,
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

}
