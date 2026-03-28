package com.timespot.backend.domain.place.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.annotation.CustomPageResponse;
import com.timespot.backend.domain.place.constant.PlaceSortType;
import com.timespot.backend.domain.place.dto.PlaceResponseDto;
import com.timespot.backend.domain.place.dto.PlaceResponseDto.SimpleAvailablePlace;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * PackageName : com.timespot.backend.domain.place.api
 * FileName    : PlaceApiDocs
 * Author      : whitecity01
 * Date        : 26. 3. 19.
 * Description : 장소 관련 API 문서화 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 19.     whitecity01       Initial creation
 * 26. 3. 22.     whitecity01       ADD pagination
 * 26. 3. 22.     whitecity01       ADD place details
 * 26. 3. 26.     whitecity01       MODIFY findAvailablePlacesOnRoute logic
 * 26. 3. 27.     whitecity01       MODIFY getPlaceDetail response
 * 26. 3. 28.     loadingKKamo21    API 문서 상세화 (응답 예시, 에러 코드, 인증 정보 추가)
 */
@Tag(
        name = "Place API",
        description = """
                      ## 장소 (Place) API
                      
                      ### 주요 기능
                      - **방문 가능 장소 조회**: 현재 위치와 남은 시간을 기반으로 방문 가능한 장소를 조회합니다.
                      - **장소 상세 정보 조회**: 특정 장소의 상세 정보 (영업시간, 이미지, 연락처 등) 를 조회합니다.
                      - **장소 검색**: 검색어와 필터 (카테고리, 정렬) 를 사용하여 장소를 검색합니다.
                      
                      ### 인증 방식
                      - **비인증 가능**: 모든 API 는 인증 없이 이용 가능합니다.
                      - **선택적 인증**: 인증 토큰이 있는 경우 더 정확한 맞춤 정보를 제공할 수 있습니다.
                      
                      ### API 경로
                      - `GET /api/v1/place` - 방문 가능 장소 조회
                      - `GET /api/v1/place/detail` - 장소 상세 정보 조회
                      - `GET /api/v1/place/search` - 장소 검색 (페이징, 필터 지원)
                      
                      ### 장소 데이터 소스
                      - **내부 DB**: 역과 매핑된 장소 정보 (위치, 카테고리, 거리)
                      - **Google Places API**: 실시간 영업정보, 이미지, 전화번호
                      """
)
public interface PlaceApiDocs {

    @Operation(
            summary = "방문 가능 장소 조회",
            description = """
                          ### 현재 사용자 위치에서 주어진 시간 내에 방문 후 복귀 가능한 장소를 조회합니다.
                          
                          #### 요청 파라미터
                          - `userLat`: 사용자 현재 위치 위도 - 필수
                          - `userLon`: 사용자 현재 위치 경도 - 필수
                          - `mapLat`: 지도 중심점 위도 - 필수
                          - `mapLon`: 지도 중심점 경도 - 필수
                          - `stationId`: 기준 역 ID - 필수
                          - `remainingMinutes`: 남은 시간 (분) - 필수
                            - 열차 출발까지 남은 시간
                            - 왕복 이동시간 + 체류시간을 고려하여 장소 필터링
                          
                          #### 처리 과정
                          1. 사용자 위치와 역 위치 계산
                          2. 도보 왕복 이동시간 계산
                          3. 남은 시간에서 이동시간을 제외한 체류 가능시간 계산
                          4. 체류 가능시간 내에 방문 가능한 장소 필터링
                          5. 거리순 정렬하여 반환
                          
                          #### 응답 데이터
                          - `placeId`: 장소 ID
                          - `lat`: 장소 위도
                          - `lon`: 장소 경도
                          - `category`: 장소 카테고리
                          - `stayableMinutes`: 체류 가능 시간 (분)
                          
                          #### 주의사항
                          - `remainingMinutes` 는 양수여야 합니다.
                          - 역과 장소 간의 도보 거리를 기반으로 계산됩니다.
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "방문 가능 장소 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PlaceResponseDto.SimpleAvailablePlace.class),
                            examples = @ExampleObject(
                                    name = "조회 성공",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "방문 가능 장소 조회가 완료되었습니다.",
                                              "data": [
                                                {
                                                  "placeId": 1,
                                                  "lat": 37.5546,
                                                  "lon": 126.9706,
                                                  "category": "카페",
                                                  "stayableMinutes": 25
                                                },
                                                {
                                                  "placeId": 2,
                                                  "lat": 37.5550,
                                                  "lon": 126.9710,
                                                  "category": "레스토랑",
                                                  "stayableMinutes": 40
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 필수 파라미터 누락 또는 유효성 검사 실패",
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
                                            name = "역 ID 없음",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "역 ID 는 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "남은 시간 없음",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "남은 시간은 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "음수 남은 시간",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "남은 시간은 양수여야 합니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "역 또는 장소를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
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
    ResponseEntity<BaseResponse<List<SimpleAvailablePlace>>> getAvailablePlaces(
            @Parameter(
                    description = "사용자 현재 위치 위도",
                    required = true,
                    example = "37.5665"
            ) @RequestParam double userLat,
            @Parameter(
                    description = "사용자 현재 위치 경도",
                    required = true,
                    example = "126.9780"
            ) @RequestParam double userLon,
            @Parameter(
                    description = "지도 중심점 위도",
                    required = true,
                    example = "37.5665"
            ) @RequestParam double mapLat,
            @Parameter(
                    description = "지도 중심점 경도",
                    required = true,
                    example = "126.9780"
            ) @RequestParam double mapLon,
            @Parameter(
                    description = "기준 역 ID",
                    required = true,
                    example = "1"
            ) @RequestParam Long stationId,
            @Parameter(
                    description = "남은 시간 (분)",
                    required = true,
                    example = "60"
            ) @Min(value = 1, message = "남은 시간은 양수여야 합니다.") @RequestParam int remainingMinutes
    );

    @Operation(
            summary = "장소 상세 정보 조회",
            description = """
                          ### 특정 장소의 상세 정보를 조회합니다.
                          
                          #### 요청 파라미터
                          - `placeId`: 조회할 장소 ID - 필수
                          - `stationId`: 기준 역 ID - 필수
                          - `userLat`: 사용자 현재 위치 위도 - 필수
                          - `userLon`: 사용자 현재 위치 경도 - 필수
                          - `remainingMinutes`: 남은 시간 (분) - 필수
                          
                          #### 응답 데이터
                          - **기본 정보**: 이름, 카테고리, 주소, 위경도
                          - **거리 정보**: 역으로부터의 직선 거리 (미터), 도보 소요 시간 (분)
                          - **시간 정보**: 체류 가능 시간, 역으로 출발해야 하는 시간
                          - **Google API 연동 정보**:
                            - 이미지 URL (대표 사진)
                            - 영업시간 (평일/주말 분리)
                            - 국제 전화번호
                          
                          #### 처리 과정
                          1. 장소 정보 조회 (내부 DB)
                          2. 역과의 거리 계산
                          3. 도보 소요 시간 계산
                          4. 체류 가능 시간 계산
                          5. Google Places API 에서 상세 정보 조회
                          6. 통합 응답 생성
                          
                          #### 주의사항
                          - Google API 연동 정보는 실시간으로 조회됩니다.
                          - 영업시간은 현지 시간 기준으로 표시됩니다.
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "장소 상세 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PlaceResponseDto.PlaceDetail.class),
                            examples = @ExampleObject(
                                    name = "조회 성공",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "장소 상세 정보 조회가 완료되었습니다.",
                                              "data": {
                                                "name": "스타벅스 서울역점",
                                                "category": "카페",
                                                "address": "서울특별시 용산구 동자동 11-111",
                                                "distanceToStation": 450,
                                                "timeToStation": 5,
                                                "stayableMinutes": 25,
                                                "stationLat": 37.5546,
                                                "stationLon": 126.9706,
                                                "leaveTime": "2026-03-27 20:30:00",
                                                "imageUrl": [
                                                  "https://places.googleapis.com/v1/places/ChIJ.../media?key=...&maxWidthPx=400"
                                                ],
                                                "weekday": [
                                                  "월요일: AM 09:00 ~ PM 06:00",
                                                  "화요일: AM 09:00 ~ PM 06:00",
                                                  "수요일: AM 09:00 ~ PM 06:00",
                                                  "목요일: AM 09:00 ~ PM 06:00",
                                                  "금요일: AM 09:00 ~ PM 06:00"
                                                ],
                                                "weekend": [
                                                  "토요일: AM 10:00 ~ PM 08:00",
                                                  "일요일: 휴무"
                                                ],
                                                "phoneNumber": "+82 2-1234-5678"
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
                                            name = "장소 ID 없음",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "장소 ID 는 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "역 ID 없음",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "역 ID 는 필수입니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "장소 또는 역을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "장소 없음",
                                            value = """
                                                    {
                                                      "code": 404,
                                                      "message": "장소를 찾을 수 없습니다."
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
    ResponseEntity<BaseResponse<PlaceResponseDto.PlaceDetail>> getPlaceDetail(
            @Parameter(
                    description = "장소 ID",
                    required = true,
                    example = "1"
            ) @RequestParam Long placeId,
            @Parameter(
                    description = "기준 역 ID",
                    required = true,
                    example = "1"
            ) @RequestParam Long stationId,
            @Parameter(
                    description = "사용자 현재 위치 위도",
                    required = true,
                    example = "37.5665"
            ) @RequestParam double userLat,
            @Parameter(
                    description = "사용자 현재 위치 경도",
                    required = true,
                    example = "126.9780"
            ) @RequestParam double userLon,
            @Parameter(
                    description = "남은 시간 (분)",
                    required = true,
                    example = "60"
            ) @Min(value = 1, message = "남은 시간은 양수여야 합니다.") @RequestParam int remainingMinutes
    );

    @Operation(
            summary = "장소 검색",
            description = """
                          ### 검색어와 필터에 맞는 장소를 검색합니다.
                          
                          #### 요청 파라미터
                          - `userLat`: 사용자 현재 위치 위도 - 필수
                          - `userLon`: 사용자 현재 위치 경도 - 필수
                          - `stationId`: 기준 역 ID - 필수
                          - `remainingMinutes`: 남은 시간 (분) - 필수
                          - `keyword`: 검색어 (장소 이름, 주소, 부분 일치) - 선택
                          - `category`: 카테고리 필터 (전체, 카페, 레스토랑, 편의점 등) - 선택 (기본값: 전체)
                          - `sortBy`: 정렬 기준 - 선택 (기본값: STATION_NEAREST)
                            - `STATION_NEAREST`: 역에서 가까운 순
                            - `USER_NEAREST`: 사용자 위치에서 가까운 순
                            - `STAYABLE_TIME_LONGEST`: 체류 가능 시간이 긴 순
                          - `markerLat`: 마커 위도 - 선택 (지도 중심점)
                          - `markerLon`: 마커 경도 - 선택 (지도 중심점)
                          - `page`: 페이지 번호 (0 부터 시작, 기본값: 0) - 선택
                          - `size`: 페이지 크기 (기본값: 10) - 선택
                          
                          #### 응답 데이터
                          - `content`: 장소 목록
                          - `number`: 현재 페이지 번호
                          - `size`: 페이지 크기
                          - `totalElements`: 전체 요소 개수 (Slice 는 제공하지 않을 수 있음)
                          - `hasNext`: 다음 페이지 존재 여부
                          
                          #### 장소 정보
                          - `placeId`: 장소 ID
                          - `name`: 장소 이름
                          - `category`: 카테고리
                          - `address`: 주소
                          - `lat`, `lon`: 위경도
                          - `stayableMinutes`: 체류 가능 시간
                          - `isOpen`: 현재 영업 중 여부
                          - `closingTime`: 금일 마감 시간
                          
                          #### 주의사항
                          - `category` 는 대소문자를 구분하지 않습니다.
                          - `keyword` 는 부분 일치를 지원합니다.
                          - `sortBy` 는 상수 값 (대문자) 을 사용해야 합니다.
                          """
    )
    @ApiResponse(
            responseCode = "200",
            description = "장소 검색 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PlaceResponseDto.SearchPlace.class),
                    examples = @ExampleObject(
                            name = "검색 성공",
                            value = """
                                    {
                                      "code": 200,
                                      "message": "장소 검색이 완료되었습니다.",
                                      "data": {
                                        "content": [
                                          {
                                            "name": "스타벅스 서울역점",
                                            "placeId": 1,
                                            "category": "카페",
                                            "address": "서울특별시 용산구 동자동 11-111",
                                            "lat": 37.5546,
                                            "lon": 126.9706,
                                            "stayableMinutes": 25,
                                            "isOpen": true,
                                            "closingTime": "2026-03-27 22:00:00"
                                          },
                                          {
                                            "name": "이디야 커피 서울역점",
                                            "placeId": 2,
                                            "category": "카페",
                                            "address": "서울특별시 용산구 동자동 11-112",
                                            "lat": 37.5550,
                                            "lon": 126.9710,
                                            "stayableMinutes": 30,
                                            "isOpen": true,
                                            "closingTime": "2026-03-27 23:00:00"
                                          }
                                        ],
                                        "number": 0,
                                        "size": 10,
                                        "hasNext": true
                                      }
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 필수 파라미터 누락 또는 유효성 검사 실패",
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
                                            name = "역 ID 없음",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "역 ID 는 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "잘못된 정렬 기준",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "정렬 기준이 올바르지 않습니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "역 또는 장소를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
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
    @CustomPageResponse(
            numberOfElements = false,
            empty = false,
            hasContent = false,
            first = false,
            last = false,
            hasPrevious = false
    )
    ResponseEntity<BaseResponse<Slice<PlaceResponseDto.SearchPlace>>> searchPlaces(
            @Parameter(
                    description = "사용자 현재 위치 위도",
                    required = true,
                    example = "37.5665"
            ) @RequestParam double userLat,
            @Parameter(
                    description = "사용자 현재 위치 경도",
                    required = true,
                    example = "126.9780"
            ) @RequestParam double userLon,
            @Parameter(
                    description = "기준 역 ID",
                    required = true,
                    example = "1"
            ) @RequestParam Long stationId,
            @Parameter(
                    description = "남은 시간 (분)",
                    required = true,
                    example = "60"
            ) @Min(value = 1, message = "남은 시간은 양수여야 합니다.") @RequestParam int remainingMinutes,
            @Parameter(
                    description = "검색어 (장소 이름, 주소, 부분 일치)",
                    required = false,
                    example = "스타벅스"
            ) @RequestParam(required = false) String keyword,
            @Parameter(
                    description = "카테고리 필터 (전체, 카페, 레스토랑, 편의점 등)",
                    required = false,
                    example = "카페"
            ) @RequestParam(required = false) String category,
            @Parameter(
                    description = "정렬 기준 (STATION_NEAREST, USER_NEAREST, STAYABLE_TIME_LONGEST)",
                    required = false,
                    example = "STATION_NEAREST"
            ) @RequestParam(defaultValue = "STATION_NEAREST") PlaceSortType sortBy,
            @Parameter(
                    description = "마커 위도 (지도 중심점)",
                    required = false,
                    example = "37.5665"
            ) @RequestParam(required = false) Double markerLat,
            @Parameter(
                    description = "마커 경도 (지도 중심점)",
                    required = false,
                    example = "126.9780"
            ) @RequestParam(required = false) Double markerLon,
            @Parameter(
                    description = "페이지 번호 (0 부터 시작)",
                    required = false,
                    example = "0"
            ) @PageableDefault(size = 10) Pageable pageable
    );

}
