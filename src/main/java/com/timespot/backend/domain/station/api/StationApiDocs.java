package com.timespot.backend.domain.station.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.annotation.CustomPageResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.favorite.dto.FavoriteResponseDto.FavoriteListResponse;
import com.timespot.backend.domain.station.dto.StationResponseDto.StationSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

/**
 * PackageName : com.timespot.backend.domain.station.api
 * FileName    : StationApiDocs
 * Author      : loadingKKamo21
 * Date        : 26. 3. 26.
 * Description : 열차/기차 역 관련 API 문서화 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 26.    whitecity01        Initial creation
 * 26. 3. 26.    loadingKKamo21     ApiDocs 완성 (상세 문서화, validation annotation 추가, radius 파라미터 추가)
 * 26. 3. 26.    loadingKKamo21     즐겨찾기 API 문서 추가 (/api/v1/stations/favorites)
 * 26. 3. 28.    loadingKKamo21     JavaDoc 주석 보강
 * 26. 3. 28.    loadingKKamo21     인증/비인증 API 분리 문서화
 */
@Tag(
        name = "Station API",
        description = """
                      ## 역 (Station) API
                      
                      ### 주요 기능
                      - **역 목록 조회**: 사용자의 위치 기반 열차/기차 역 목록 통합 조회 (즐겨찾기 + 근처 + 전체)
                      - **즐겨찾기 관리**: 열차/기차 역 즐겨찾기 추가/삭제/목록 조회
                      
                      ### API 경로
                      - `GET /api/v1/stations` - 역 목록 통합 조회
                      - `POST /api/v1/stations/favorites/{stationId}` - 즐겨찾기 역 추가
                      - `GET /api/v1/stations/favorites` - 즐겨찾기 역 목록 조회
                      - `DELETE /api/v1/stations/favorites/{stationId}` - 즐겨찾기 역 삭제
                      
                      ### 인증 방식
                      - **역 목록 조회**: 인증 없이 이용 가능 (인증 시 즐겨찾기 정보 포함)
                      - **즐겨찾기 관리**: `Bearer Token` 인증 필요
                      - 요청 헤더에 `Authorization: Bearer {accessToken}` 를 포함해야 합니다.
                      
                      ### 대상 역 정보
                      - **지하철이 아닌 열차/기차 역**을 대상으로 합니다
                      - 서울역, 용산역, 부산역 등 KTX/일반열차 역 정보 제공
                      """
)
public interface StationApiDocs {

    @Operation(
            summary = "역 목록 통합 조회",
            description = """
                          ### 사용자의 위치를 기반으로 역 목록을 통합 조회합니다.
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 선택 (인증 시 즐겨찾기 여부 포함)
                          
                          #### 쿼리 파라미터
                          - `userLat`: 사용자 위도 - 필수
                          - `userLon`: 사용자 경도 - 필수
                          - `radius`: 검색 반경 (미터 단위, 기본값: 2000, 최대: 10000) - 선택
                          - `keyword`: 검색어 (역 이름 또는 주소, 부분 일치) - 선택
                          - `page`: 페이지 번호 (1 부터 시작, 기본값: 1) - 선택
                          - `size`: 페이지 크기 (한 페이지당 요소 개수, 최소 10, 기본값: 10) - 선택
                          - `sort`: 정렬 기준 (프로퍼티,방향 - 쉼표로 여러 개 지정 가능) - 선택
                            - **프로퍼티**: `stationName`
                            - **방향**: `ASC`, `DESC` (대소문자 구분 없음)
                            - **단일 정렬 예시**: `stationName,ASC`
                            - **다중 정렬 예시**: `stationName,DESC,stationName,ASC`
                          
                          #### 응답 데이터
                          - `favoriteStations`: 사용자가 즐겨찾기한 역 목록 (인증 시에만 populated, 아니면 빈 배열)
                          - `nearbyStations`: 사용자 위치와 가까운 역 목록 (거리 기준 오름차순, 최대 5 개)
                          - `stations`: 전체 역 목록 (페이징 적용, 정렬 가능)
                          
                          #### 주의사항
                          - 인증하지 않은 사용자는 `favoriteStations` 가 빈 배열로 반환됩니다.
                          - `nearbyStations` 는 거리 기준 오름차순 정렬됩니다 (최대 5 개).
                          - `stations` 은 페이징 처리되며, 기본 정렬은 `stationName,ASC` 입니다.
                          """
    )
    @ApiResponse(
            responseCode = "200",
            description = "역 목록 통합 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = StationSearchResponse.class),
                    examples = @ExampleObject(
                            name = "조회 성공",
                            value = """
                                    {
                                      "code": 200,
                                      "message": "역 정보를 성공적으로 조회했습니다.",
                                      "data": {
                                        "favoriteStations": [
                                          {
                                            "stationId": 1,
                                            "name": "서울역",
                                            "lines": ["경부선", "호남선"]
                                          }
                                        ],
                                        "nearbyStations": [
                                          {
                                            "stationId": 1,
                                            "name": "서울역",
                                            "lines": ["경부선", "호남선"]
                                          },
                                          {
                                            "stationId": 2,
                                            "name": "시청역",
                                            "lines": ["1 호선", "2 호선"]
                                          }
                                        ],
                                        "stations": {
                                          "content": [
                                            {
                                              "stationId": 1,
                                              "name": "서울역",
                                              "lines": ["경부선", "호남선"]
                                            }
                                          ],
                                          "number": 0,
                                          "size": 10,
                                          "totalElements": 500,
                                          "totalPages": 50,
                                          "first": true,
                                          "last": false,
                                          "hasNext": true,
                                          "hasPrevious": false
                                        }
                                      }
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 위도/경도 없음 또는 파라미터 유효성 검사 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "위도/경도 없음",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "위도와 경도는 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "반경 초과",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "검색 반경은 최대 10000m 입니다."
                                                    }
                                                    """
                                    ),
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
                                                      "message": "정렬 형식이 올바르지 않습니다. (예: stationName,ASC)"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @CustomPageResponse(
            numberOfElements = false,
            empty = false,
            hasContent = false,
            first = false,
            last = false,
            hasPrevious = false
    )
    ResponseEntity<BaseResponse<StationSearchResponse>> getStations(
            @Parameter(
                    description = "사용자 위도",
                    required = true,
                    example = "37.5665"
            ) double userLat,
            @Parameter(
                    description = "사용자 경도",
                    required = true,
                    example = "126.9780"
            ) double userLon,
            @Parameter(
                    description = "검색 반경 (미터 단위, 기본값: 2000, 범위: 1~10000)",
                    example = "2000",
                    required = false
            ) @Min(value = 1, message = "검색 반경은 1m 이상이어야 합니다.")
            @Max(value = 10000, message = "검색 반경은 최대 10000m 입니다.") double radius,
            @Parameter(
                    description = "검색어 (역 이름 또는 주소, 부분 일치)",
                    example = "서울",
                    required = false
            ) String keyword,
            @Parameter(
                    description = "페이지 번호 (1 부터 시작)",
                    example = "1",
                    required = false
            ) @Min(1) int page,
            @Parameter(
                    description = "페이지 크기 (한 페이지당 요소 개수, 최소 10)",
                    example = "10",
                    required = false
            ) @Min(10) int size,
            @Parameter(
                    description = """
                                  정렬 기준 (프로퍼티,방향) - 쉼표로 여러 개 지정 가능
                                  - 프로퍼티: stationName
                                  - 방향: ASC, DESC (대소문자 구분 없음)
                                  - 예시: `stationName,ASC` 또는 `stationName,DESC`
                                  """,
                    example = "stationName,ASC",
                    required = false
            ) @Pattern(
                    regexp = "^stationName,(ASC|DESC|asc|desc)(,\\s*stationName,(ASC|DESC|asc|desc))*$",
                    message = "정렬 형식이 올바르지 않습니다. (예: stationName,ASC)"
            ) String sort,
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(
            summary = "즐겨찾기 역 추가",
            description = """
                          ### 사용자가 특정 역을 즐겨찾기에 추가합니다.
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 필수
                          
                          #### 요청 본문
                          - `stationId`: 추가할 역 ID - 필수
                          
                          #### 요청 URL
                          - `POST /api/v1/stations/favorites/{stationId}`
                          
                          #### 처리 과정
                          1. 사용자 존재 여부 확인
                          2. 역 존재 여부 확인
                          3. 중복 즐겨찾기 검증
                          4. 즐겨찾기 생성 및 저장
                          
                          #### 주의사항
                          - 이미 즐겨찾기에 등록된 역은 중복 추가할 수 없습니다.
                          - 중복 시도 시 `409 CONFLICT` 에러가 반환됩니다.
                          """
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "즐겨찾기 추가 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "추가 성공",
                                    value = """
                                            {
                                              "code": 201,
                                              "message": "즐겨찾기 역이 성공적으로 생성되었습니다."
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
                                            name = "존재하지 않는 역",
                                            value = """
                                                    {
                                                      "code": 404,
                                                      "message": "역을 찾을 수 없습니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자 또는 역을 찾을 수 없음",
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
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "중복된 즐겨찾기",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "중복 오류",
                                    value = """
                                            {
                                              "code": 409,
                                              "message": "이미 즐겨찾기에 등록된 역입니다."
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<BaseResponse<Void>> createFavoriteStation(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(
                    description = "역 ID",
                    required = true,
                    example = "1"
            ) @Min(1) Long stationId
    );

    @Operation(
            summary = "즐겨찾기 역 삭제",
            description = """
                          ### 역 ID 를 사용하여 특정 즐겨찾기를 삭제합니다.
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 필수
                          
                          #### 경로 변수
                          - `stationId`: 삭제할 즐겨찾기 역 ID - 필수
                          
                          #### 요청 URL
                          - `DELETE /api/v1/stations/favorites/{stationId}`
                          
                          #### 처리 과정
                          1. 즐겨찾기 존재 여부 확인
                          2. 본인 소유 검증 (보안)
                          3. 즐겨찾기 삭제
                          
                          #### 중요
                          - 본인의 즐겨찾기가 아닌 경우 `404` 에러가 반환됩니다 (보안).
                          """
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "즐겨찾기 삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "삭제 성공",
                                    value = """
                                            {
                                              "code": 204,
                                              "message": "즐겨찾기 역이 성공적으로 삭제되었습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "즐겨찾기를 찾을 수 없음 또는 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "즐겨찾기 없음",
                                            value = """
                                                    {
                                                      "code": 404,
                                                      "message": "즐겨찾기를 찾을 수 없습니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "권한 없음 (타인의 즐겨찾기)",
                                            value = """
                                                    {
                                                      "code": 404,
                                                      "message": "즐겨찾기를 찾을 수 없습니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<BaseResponse<Void>> deleteFavoriteStation(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(
                    description = "역 ID",
                    required = true,
                    example = "1"
            ) @Min(1) Long stationId
    );

    @Operation(
            summary = "즐겨찾기 역 목록 조회",
            description = """
                          ### 사용자의 즐겨찾기 역 목록을 조회합니다.
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 필수
                          
                          #### 쿼리 파라미터
                          - `keyword`: 검색어 (역 이름, 부분 일치, 대소문자 구분 없음) - 선택
                          - `page`: 페이지 번호 (1 부터 시작, 기본값: 1) - 선택
                          - `size`: 페이지 크기 (최소 10, 기본값: 10) - 선택
                          - `sort`: 정렬 기준 (프로퍼티,방향 - 쉼표로 여러 개 지정 가능) - 선택
                            - **프로퍼티**: `createdAt`, `stationName`, `visitCount`
                            - **방향**: `ASC`, `DESC` (대소문자 구분 없음)
                            - **단일 정렬 예시**: `createdAt,DESC`
                            - **다중 정렬 예시**: `visitCount,DESC,stationName,ASC`
                          
                          #### 요청 URL
                          - `GET /api/v1/stations/favorites`
                          
                          #### 응답 데이터
                          - `favoriteId`: 즐겨찾기 ID
                          - `stationId`: 역 ID
                          - `stationName`: 역 이름
                          - `visitCount`: 방문 횟수 (정상 완료된 여정만 카운트)
                          - `totalVisitMinutes`: 총 방문 시간 (분) (정상 완료된 여정의 소요 시간 합계)
                          - `createdAt`: 즐겨찾기 추가 일시
                          
                          #### 페이징 정보
                          - `content`: 즐겨찾기 목록
                          - `totalElements`: 전체 요소 개수
                          - `totalPages`: 전체 페이지 수
                          - `size`: 페이지 크기
                          - `number`: 현재 페이지 번호
                          - `sort`: 정렬 정보
                          - `hasNext`: 다음 페이지 존재 여부
                          """
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponse(
            responseCode = "200",
            description = "즐겨찾기 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = FavoriteListResponse.class),
                    examples = @ExampleObject(
                            name = "조회 성공",
                            value = """
                                    {
                                      "code": 200,
                                      "message": "즐겨찾기 역 목록 조회가 완료되었습니다.",
                                      "data": {
                                        "content": [
                                          {
                                            "favoriteId": 1,
                                            "stationId": 10,
                                            "stationName": "서울역",
                                            "visitCount": 5,
                                            "totalVisitMinutes": 150,
                                            "createdAt": "2024-03-24T16:00:00"
                                          },
                                          {
                                            "favoriteId": 2,
                                            "stationId": 20,
                                            "stationName": "강남역",
                                            "visitCount": 3,
                                            "totalVisitMinutes": 75,
                                            "createdAt": "2024-03-23T10:30:00"
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
                                                      "message": "정렬 형식이 올바르지 않습니다. (예: createdAt,DESC 또는 visitCount,DESC,stationName,ASC)"
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
    @CustomPageResponse(
            numberOfElements = false,
            empty = false,
            hasContent = false,
            first = false,
            last = false,
            hasPrevious = false
    )
    ResponseEntity<BaseResponse<Page<FavoriteListResponse>>> getFavoriteStationList(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(
                    description = "검색어 (역 이름, 부분 일치, 대소문자 구분 없음)",
                    example = "서울",
                    required = false
            ) String keyword,
            @Parameter(
                    description = "페이지 번호 (1 부터 시작)",
                    example = "1",
                    required = false
            ) @Min(1) int page,
            @Parameter(
                    description = "페이지 크기 (한 페이지당 요소 개수, 최소 10)",
                    example = "10",
                    required = false
            ) @Min(10) int size,
            @Parameter(
                    description = """
                                  정렬 기준 (프로퍼티,방향) - 쉼표로 여러 개 지정 가능
                                  - 프로퍼티: createdAt, stationName, visitCount
                                  - 방향: ASC, DESC (대소문자 구분 없음)
                                  - 예시: `createdAt,DESC` 또는 `visitCount,DESC,stationName,ASC`
                                  """,
                    example = "createdAt,DESC",
                    required = false
            ) @Pattern(
                    regexp = "^(createdAt|stationName|visitCount),(ASC|DESC|asc|desc)(,\\s*" +
                             "(createdAt|stationName|visitCount),(ASC|DESC|asc|desc))*$",
                    message = "정렬 형식이 올바르지 않습니다. (예: createdAt,DESC 또는 visitCount,DESC,stationName,ASC)"
            ) String sort
    );

}
