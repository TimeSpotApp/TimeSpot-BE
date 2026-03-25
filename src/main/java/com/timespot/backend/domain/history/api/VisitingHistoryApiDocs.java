package com.timespot.backend.domain.history.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.history.dto.VisitingHistoryRequestDto.JourneyEndRequest;
import com.timespot.backend.domain.history.dto.VisitingHistoryRequestDto.JourneyStartRequest;
import com.timespot.backend.domain.history.dto.VisitingHistoryResponseDto.VisitingHistoryDetailResponse;
import com.timespot.backend.domain.history.dto.VisitingHistoryResponseDto.VisitingHistoryListResponse;
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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

/**
 * PackageName : com.timespot.backend.domain.history.api
 * FileName    : VisitingHistoryApiDocs
 * Author      : loadingKKamo21
 * Date        : 26. 3. 26.
 * Description : 방문 이력 관련 API 문서화 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 26.    loadingKKamo21       Initial creation
 */
@Tag(
        name = "Visiting History API",
        description = """
                      ## 방문 이력 (여정) 관리 API
                      
                      사용자의 지하철 역 방문 여정을 기록하고 관리하는 기능을 제공합니다.
                      
                      ### 인증 방식
                      - 모든 API 는 `Bearer Token` 인증이 필요합니다.
                      - 요청 헤더에 `Authorization: Bearer {accessToken}` 를 포함해야 합니다.
                      
                      ### 주요 기능
                      - **여정 시작**: 특정 역에서 방문 기록을 시작합니다.
                      - **여정 종료**: 진행 중인 여정을 종료하고 성공/실패를 기록합니다.
                      - **여정 목록**: 사용자의 방문 이력 목록을 조회합니다 (페이징, 검색 지원).
                      
                      ### 여정 기록 흐름
                      1. **여정 시작**: 역과 장소를 선택하여 여정 기록 시작
                      2. **여정 진행**: 실제 방문 활동 수행
                      3. **여정 종료**: 
                         - 완료 (`isCompleted: true`): 열차 출발 전 도착 여부 자동 판별
                         - 포기 (`isCompleted: false`): 중도 포기 처리
                      """
)
@SecurityRequirement(name = "BearerAuth")
public interface VisitingHistoryApiDocs {

    @Operation(
            summary = "여정 시작 (새로운 방문 이력 생성)",
            description = """
                          ### 사용자가 특정 역에서 새로운 여정을 시작합니다.
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 필수
                          
                          #### 요청 본문
                          - `stationId`: 출발 역 ID - 필수
                          - `placeId`: 방문 장소 ID - 필수
                          - `trainDepartureTime`: 열차 출발 시간 (ISO-8601 형식) - 필수
                          
                          #### 처리 과정
                          1. 사용자 존재 여부 확인
                          2. 역 존재 여부 확인
                          3. 장소 존재 여부 확인
                          4. 방문 이력 생성 및 저장
                          5. 현재 시간을 시작 시간으로 자동 설정
                          
                          #### 응답 데이터
                          - `visitingHistoryId`: 생성된 방문 이력 ID
                          - `stationId`: 역 ID
                          - `stationName`: 역 이름
                          - `placeId`: 장소 ID
                          - `placeName`: 장소 이름
                          - `startTime`: 여정 시작 시간 (현재 시간)
                          - `trainDepartureTime`: 열차 출발 시간
                          - `isInProgress`: 진행 중 여부 (true)
                          - `isSuccess`: 여정 성공 여부 (종료 시 자동 판별)
                          
                          #### 주의사항
                          - 시작 시간은 서버 현재 시간으로 자동 설정됩니다.
                          - 열차 출발 시간은 시작 시간보다 이후여야 합니다.
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "여정 시작 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VisitingHistoryDetailResponse.class),
                            examples = @ExampleObject(
                                    name = "여정 시작 성공",
                                    value = """
                                            {
                                              "code": 201,
                                              "message": "새로운 여정이 시작되었습니다.",
                                              "data": {
                                                "visitingHistoryId": 1,
                                                "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                                "stationId": 10,
                                                "stationName": "서울역",
                                                "stationAddress": "서울특별시 용산구 청암로 92",
                                                "placeId": 100,
                                                "placeName": "스타벅스 서울역점",
                                                "placeCategory": "카페",
                                                "placeAddress": "서울특별시 용산구 청암로 90 1 층",
                                                "startTime": "2024-03-25T13:00:00",
                                                "endTime": null,
                                                "trainDepartureTime": "2024-03-25T15:30:00",
                                                "totalDurationMinutes": 0,
                                                "isInProgress": true,
                                                "isSuccess": false,
                                                "createdAt": "2024-03-25T13:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "역 ID 없음",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "역 ID 는 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "장소 ID 없음",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "장소 ID 는 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "열차 출발 시간 없음",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "열차 출발 시간은 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "잘못된 열차 출발 시간",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "종료 시간은 시작 시간보다 이후여야 합니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자, 역 또는 장소를 찾을 수 없음",
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
                                    ),
                                    @ExampleObject(
                                            name = "장소 없음",
                                            value = """
                                                    {
                                                      "code": 404,
                                                      "message": "장소를 찾을 수 없습니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<BaseResponse<VisitingHistoryDetailResponse>> createNewJourney(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(
                    description = "여정 시작 요청 페이로드",
                    required = true
            ) @Valid JourneyStartRequest dto
    );

    @Operation(
            summary = "여정 종료 (방문 이력 완료 또는 포기)",
            description = """
                          ### 진행 중인 여정을 종료합니다.
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 필수
                          
                          #### 경로 변수
                          - `historyId`: 종료할 여정의 방문 이력 ID - 필수
                          
                          #### 요청 본문
                          - `isCompleted`: 여정 완료 여부 - 필수
                            - `true`: 정상 완료 (열차 출발 전 도착 여부 자동 판별)
                            - `false`: 중도 포기 (성공 여부 false 로 설정)
                          
                          #### 처리 과정
                          1. 사용자 존재 여부 확인
                          2. 방문 이력 존재 여부 확인
                          3. 현재 시간을 종료 시간으로 설정
                          4. 성공 여부 자동 판별 (종료 시간 ≤ 열차 출발 시간)
                          5. 소요 시간 계산 및 저장
                          
                          #### 응답 데이터
                          - `endTime`: 여정 종료 시간 (현재 시간)
                          - `totalDurationMinutes`: 총 소요 시간 (분)
                          - `isInProgress`: 진행 중 여부 (false)
                          - `isSuccess`: 여정 성공 여부 (열차 출발 전 도착 시 true)
                          
                          #### 성공 판별 기준
                          - `endTime ≤ trainDepartureTime`: 성공 (true)
                          - `endTime > trainDepartureTime`: 실패 (false)
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "여정 종료 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VisitingHistoryDetailResponse.class),
                            examples = {@ExampleObject(
                                    name = "여정 종료 성공 (성공)",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "여정이 종료되었습니다.",
                                              "data": {
                                                "visitingHistoryId": 1,
                                                "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                                "stationId": 10,
                                                "stationName": "서울역",
                                                "stationAddress": "서울특별시 용산구 청암로 92",
                                                "placeId": 100,
                                                "placeName": "스타벅스 서울역점",
                                                "placeCategory": "카페",
                                                "placeAddress": "서울특별시 용산구 청암로 90 1 층",
                                                "startTime": "2024-03-25T13:00:00",
                                                "endTime": "2024-03-25T14:30:00",
                                                "trainDepartureTime": "2024-03-25T15:30:00",
                                                "totalDurationMinutes": 90,
                                                "isInProgress": false,
                                                "isSuccess": true,
                                                "createdAt": "2024-03-25T13:00:00"
                                              }
                                            }
                                            """
                            ),
                                        @ExampleObject(
                                                name = "여정 종료 성공 (실패 - 시간 초과)",
                                                value = """
                                                        {
                                                          "code": 200,
                                                          "message": "여정이 종료되었습니다.",
                                                          "data": {
                                                            "visitingHistoryId": 1,
                                                            "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                                            "stationId": 10,
                                                            "stationName": "서울역",
                                                            "stationAddress": "서울특별시 용산구 청암로 92",
                                                            "placeId": 100,
                                                            "placeName": "스타벅스 서울역점",
                                                            "placeCategory": "카페",
                                                            "placeAddress": "서울특별시 용산구 청암로 90 1 층",
                                                            "startTime": "2024-03-25T13:00:00",
                                                            "endTime": "2024-03-25T16:00:00",
                                                            "trainDepartureTime": "2024-03-25T15:30:00",
                                                            "totalDurationMinutes": 180,
                                                            "isInProgress": false,
                                                            "isSuccess": false,
                                                            "createdAt": "2024-03-25T13:00:00"
                                                          }
                                                        }
                                                        """
                                        )}
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "완료 여부 없음",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "여정 완료 여부는 필수입니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자 또는 방문 이력을 찾을 수 없음",
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
                                            name = "방문 이력 없음",
                                            value = """
                                                    {
                                                      "code": 404,
                                                      "message": "방문 이력을 찾을 수 없습니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<BaseResponse<VisitingHistoryDetailResponse>> endJourney(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(
                    description = "방문 이력 ID",
                    required = true,
                    example = "1"
            ) @Min(1) Long historyId,
            @Parameter(
                    description = "여정 종료 요청 페이로드",
                    required = true
            ) @Valid JourneyEndRequest dto
    );

    @Operation(
            summary = "방문 이력 목록 조회",
            description = """
                          ### 사용자의 방문 이력 목록을 조회합니다.
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 필수
                          
                          #### 쿼리 파라미터
                          - `keyword`: 검색어 (역 이름, 역 주소, 장소 이름, 장소 주소, 부분 일치) - 선택
                          - `page`: 페이지 번호 (1 부터 시작, 기본값: 1) - 선택
                          - `size`: 페이지 크기 (최소 10, 기본값: 10) - 선택
                          - `sort`: 정렬 기준 (프로퍼티,방향 - 쉼표로 여러 개 지정 가능) - 선택
                            - **프로퍼티**: `createdAt`, `duration` (소요 시간)
                            - **방향**: `ASC`, `DESC` (대소문자 구분 없음)
                            - **단일 정렬 예시**: `createdAt,DESC`
                            - **다중 정렬 예시**: `duration,DESC,createdAt,ASC`
                          
                          #### 응답 데이터
                          - `visitingHistoryId`: 방문 이력 ID
                          - `stationId`: 역 ID
                          - `stationName`: 역 이름
                          - `placeId`: 장소 ID
                          - `placeName`: 장소 이름
                          - `placeCategory`: 장소 카테고리
                          - `startTime`: 여정 시작 시간
                          - `endTime`: 여정 종료 시간 (null 인 경우 진행 중)
                          - `trainDepartureTime`: 열차 출발 시간
                          - `totalDurationMinutes`: 총 소요 시간 (분)
                          - `isInProgress`: 진행 중 여부
                          - `isSuccess`: 여정 성공 여부
                          
                          #### 페이징 정보
                          - `content`: 방문 이력 목록
                          - `totalElements`: 전체 요소 개수
                          - `totalPages`: 전체 페이지 수
                          - `size`: 페이지 크기
                          - `number`: 현재 페이지 번호
                          - `sort`: 정렬 정보
                          - `hasNext`: 다음 페이지 존재 여부
                          """
    )
    @ApiResponse(
            responseCode = "200",
            description = "방문 이력 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = VisitingHistoryListResponse.class),
                    examples = @ExampleObject(
                            name = "조회 성공",
                            value = """
                                    {
                                      "code": 200,
                                      "message": "방문 이력 목록 조회가 완료되었습니다.",
                                      "data": {
                                        "content": [
                                          {
                                            "visitingHistoryId": 1,
                                            "stationId": 10,
                                            "stationName": "서울역",
                                            "placeId": 100,
                                            "placeName": "스타벅스 서울역점",
                                            "placeCategory": "카페",
                                            "startTime": "2024-03-25T13:00:00",
                                            "endTime": "2024-03-25T14:30:00",
                                            "trainDepartureTime": "2024-03-25T15:30:00",
                                            "totalDurationMinutes": 90,
                                            "isInProgress": false,
                                            "isSuccess": true,
                                            "createdAt": "2024-03-25T13:00:00"
                                          },
                                          {
                                            "visitingHistoryId": 2,
                                            "stationId": 20,
                                            "stationName": "강남역",
                                            "placeId": 200,
                                            "placeName": "강남역 맛집",
                                            "placeCategory": "레스토랑",
                                            "startTime": "2024-03-24T10:00:00",
                                            "endTime": null,
                                            "trainDepartureTime": "2024-03-24T12:00:00",
                                            "totalDurationMinutes": 0,
                                            "isInProgress": true,
                                            "isSuccess": false,
                                            "createdAt": "2024-03-24T10:00:00"
                                          }
                                        ],
                                        "totalElements": 15,
                                        "totalPages": 2,
                                        "size": 10,
                                        "number": 0,
                                        "first": true,
                                        "last": false
                                      }
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 파라미터 유효성 검사 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "페이지 번호 오류",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "페이지 번호는 1 이상이어야 합니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "페이지 크기 오류",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "페이지 크기는 최소 10 이상이어야 합니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "정렬 형식 오류",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "정렬 형식이 올바르지 않습니다. (예: createdAt,DESC 또는 duration,DESC)"
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
            )
    })
    ResponseEntity<BaseResponse<Page<VisitingHistoryListResponse>>> getVisitingHistoryList(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(
                    description = "검색어 (역 이름, 주소, 장소 이름, 주소)"
            ) String keyword,
            @Parameter(
                    description = "페이지 번호 (1 부터 시작)"
            ) @Min(1) int page,
            @Parameter(
                    description = "페이지 크기 (최소 10)"
            ) @Min(10) int size,
            @Parameter(
                    description = "정렬 기준 (프로퍼티,방향 - 쉼표로 여러 개 지정 가능)"
            ) @Pattern(regexp = "^(createdAt|duration),(ASC|DESC|asc|desc)(,\\s*(createdAt|duration),(ASC|DESC|asc|desc))*$") String sort
    );

}
