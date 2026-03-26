package com.timespot.backend.domain.favorite.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.favorite.dto.FavoriteRequestDto.FavoriteStationCreateRequest;
import com.timespot.backend.domain.favorite.dto.FavoriteResponseDto.FavoriteListResponse;
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
 * PackageName : com.timespot.backend.domain.favorite.api
 * FileName    : FavoriteApiDocs
 * Author      : loadingKKamo21
 * Date        : 26. 3. 25.
 * Description : 즐겨찾기 관련 API 문서화 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 25.    loadingKKamo21       Initial creation
 */
@Tag(
        name = "Favorite API",
        description = """
                      ## 즐겨찾기 관리 API
                      
                      사용자가 역을 즐겨찾기에 추가하고 관리하는 기능을 제공합니다.
                      
                      ### 인증 방식
                      - 모든 API 는 `Bearer Token` 인증이 필요합니다.
                      - 요청 헤더에 `Authorization: Bearer {accessToken}` 를 포함해야 합니다.
                      
                      ### 주요 기능
                      - **즐겨찾기 추가**: 특정 역을 즐겨찾기에 추가합니다.
                      - **즐겨찾기 삭제**: 즐겨찾기 ID 로 특정 즐겨찾기를 삭제합니다.
                      - **즐겨찾기 목록**: 사용자의 즐겨찾기 역 목록을 조회합니다 (페이징, 검색 지원).
                      
                      ### 삭제 시 주의사항
                      - 삭제 시 `stationId` 가 아닌 `favoriteId` 를 사용합니다.
                      - 이는 REST 원칙에 따라 리소스를 명확히 식별하기 위함입니다.
                      - 본인의 즐겨찾기만 삭제할 수 있습니다 (보안 검증).
                      """
)
@SecurityRequirement(name = "BearerAuth")
public interface FavoriteApiDocs {

    @Operation(
            summary = "즐겨찾기 역 추가",
            description = """
                          ### 사용자가 특정 역을 즐겨찾기에 추가합니다.
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 필수
                          
                          #### 요청 본문
                          - `stationId`: 추가할 역 ID - 필수
                          
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
                                              "message": "즐겨찾기가 성공적으로 생성되었습니다."
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
                    description = "즐겨찾기 역 생성 요청 페이로드",
                    required = true
            ) @Valid FavoriteStationCreateRequest dto
    );

    @Operation(
            summary = "즐겨찾기 역 삭제",
            description = """
                          ### 즐겨찾기 ID 를 사용하여 특정 즐겨찾기를 삭제합니다.
                          
                          #### 요청 헤더
                          - `Authorization: Bearer {accessToken}` - 필수
                          
                          #### 경로 변수
                          - `favoriteId`: 삭제할 즐겨찾기 ID - 필수
                          
                          #### 처리 과정
                          1. 즐겨찾기 존재 여부 확인
                          2. 본인 소유 검증 (보안)
                          3. 즐겨찾기 삭제
                          
                          #### 중요
                          - `stationId` 가 아닌 `favoriteId` 를 사용합니다.
                          - 이는 REST 원칙에 따라 리소스를 명확히 식별하기 위함입니다.
                          - 본인의 즐겨찾기가 아닌 경우 `404` 에러가 반환됩니다 (보안).
                          """
    )
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
                                              "message": "즐겨찾기가 성공적으로 삭제되었습니다."
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
                    description = "즐겨찾기 ID",
                    required = true,
                    example = "1"
            ) @Min(1) Long favoriteId
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
                          
                          #### 응답 데이터
                          - `favoriteId`: 즐겨찾기 ID
                          - `stationId`: 역 ID
                          - `stationName`: 역 이름
                          - `visitCount`: 방문 횟수
                          - `createdAt`: 즐겨찾기 추가 일시
                          
                          #### 페이징 정보
                          - `content`: 즐겨찾기 목록
                          - `totalElements`: 전체 요소 개수
                          - `totalPages`: 전체 페이지 수
                          - `size`: 페이지 크기
                          - `number`: 현재 페이지 번호
                          - `sort`: 정렬 정보
                          - `haxNext`: 다음 페이지 존재 여부
                          """
    )
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
                                      "message": "즐겨찾기를 성공적으로 조회했습니다.",
                                      "data": {
                                        "content": [
                                          {
                                            "favoriteId": 1,
                                            "stationId": 10,
                                            "stationName": "서울역",
                                            "visitCount": 5,
                                            "createdAt": "2024-03-24T16:00:00"
                                          },
                                          {
                                            "favoriteId": 2,
                                            "stationId": 20,
                                            "stationName": "강남역",
                                            "visitCount": 3,
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
