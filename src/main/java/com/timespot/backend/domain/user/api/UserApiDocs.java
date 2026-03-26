package com.timespot.backend.domain.user.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.security.dto.AuthResponseDto.AuthInfoResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
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
                      
                      회원 정보 조회, 수정, 탈퇴 기능을 제공합니다.
                      
                      ### 인증 방식
                      - 모든 API 는 `Bearer Token` 인증이 필요합니다.
                      - 요청 헤더에 `Authorization: Bearer {accessToken}` 를 포함해야 합니다.
                      
                      ### 주요 기능
                      - **회원 정보 조회**: 현재 로그인한 사용자의 정보를 조회합니다.
                      - **회원 정보 수정**: 주사용 지도 API 를 변경합니다.
                      - **회원 탈퇴**: 사용자를 탈퇴 처리하고 모든 연동 정보를 삭제합니다.
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

}
