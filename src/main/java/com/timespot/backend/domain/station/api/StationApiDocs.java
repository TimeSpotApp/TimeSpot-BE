package com.timespot.backend.domain.station.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.annotation.CustomPageResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.station.dto.StationResponseDto.StationSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;

/**
 * PackageName : com.timespot.backend.domain.station.api
 * FileName    : StationApiDocs
 * Author      : loadingKKamo21
 * Date        : 26. 3. 26.
 * Description : 역 관련 API 문서화 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 26.    whitecity01        Initial creation
 * 26. 3. 26.    loadingKKamo21     ApiDocs 완성 (상세 문서화, validation annotation 추가, radius 파라미터 추가)
 */
@Tag(name = "Station API", description = "역 API")
public interface StationApiDocs {

    @Operation(
            summary = "역 목록 통합 조회",
            description = """
                          ### 사용자의 위치를 기반으로 역 목록을 통합 조회합니다.
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 선택 (인증 시 즐겨찾기 여부 포함)
                          
                          #### 쿼리 파라미터
                          - `lat`: 사용자 위도 - 필수
                          - `lng`: 사용자 경도 - 필수
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
                                          "totalElements": 500,
                                          "totalPages": 50,
                                          "size": 10,
                                          "number": 0,
                                          "first": true,
                                          "last": false
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
            ) double lat,
            @Parameter(
                    description = "사용자 경도",
                    required = true,
                    example = "126.9780"
            ) double lng,
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

    /*
    @Operation(summary = "역 조회", description = "사용자 즐겨찾기 역, 가까운 역, 모든 역 정보를 제공합니다.")
    @ApiResponse(responseCode = "200", description = "역 조회 완료")
    ResponseEntity<BaseResponse<StationList>> getStations(
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );
    */

}
