package com.timespot.backend.domain.place.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.annotation.CustomPageResponse;
import com.timespot.backend.domain.place.dto.PlaceResponseDtoV2.AvailablePlace;
import com.timespot.backend.domain.place.dto.PlaceResponseDtoV2.PlaceDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * PackageName : com.timespot.backend.domain.place.api
 * FileName    : PlaceApiDocsV2
 * Author      : loadingKKamo21
 * Date        : 26. 3. 29.
 * Description : Place API V2 Swagger 문서 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 29.    loadingKKamo21       Initial creation
 * 26. 3. 30.    loadingKKamo21       API 문서 상세화 (응답 예시, 에러 코드, 파라미터 설명 추가)
 */
@Tag(
        name = "Place V2 API",
        description = """
                      ## 장소 (Place) API V2

                      ### 개요
                      VisitKorea(한국관광공사) 공공데이터를 기반으로, 역에서 방문 가능한 장소 정보를 제공합니다.

                      ### 주요 기능
                      - **방문 가능 장소 목록 조회**: 사용자 위치, 역, 남은 시간을 기반으로 방문 가능한 장소를 조회합니다.
                      - **장소 상세 정보 조회**: 특정 장소의 상세 정보 (이미지, 휴무일, 이용시간, 타입별 특화 정보) 를 조회합니다.

                      ### 인증 방식
                      - **비인증 가능**: 모든 API 는 인증 없이 이용 가능합니다.

                      ### 데이터 소스
                      - **내부 DB**: 역 정보, 장소 위치 (위경도), 카테고리
                      - **VisitKorea API**: 장소 상세 정보, 이미지, 휴무일, 이용시간

                      ### 캐싱 전략
                      - **Redis GEO**: 역 기준 반경 3km 이내 장소 저장 (24 시간 TTL)
                      - **PlaceCardCache**: 장소 기본 정보 캐시 (24 시간 TTL)
                      - **PlaceDetailCache**: 장소 상세 정보 캐시 (7 일 TTL)
                      - **동기화**: 캐시 미스 시 VisitKorea API 에서 실시간 동기화
                      """
)
public interface PlaceApiDocsV2 {

    @Operation(
            summary = "방문 가능 장소 목록 조회",
            description = """
                          ### 사용자 위치와 역, 남은 시간을 입력받아 방문 가능한 장소 목록을 조회합니다.

                          #### 요청 파라미터
                          - `stationId`: 출발 역 ID - 필수
                          - `userLat`: 사용자 현재 위치 위도 - 필수
                          - `userLon`: 사용자 현재 위치 경도 - 필수
                          - `remainingMinutes`: 열차 출발까지 남은 시간 (분) - 필수
                          - `mapLat`: 지도 중심 위도 - 선택 (지도 중심 정렬 사용 시 필수)
                          - `mapLon`: 지도 중심 경도 - 선택 (지도 중심 정렬 사용 시 필수)
                          - `keyword`: 검색 키워드 (장소명, 카테고리 부분 일치) - 선택
                          - `category`: 카테고리 필터 - 선택 (기본값: 전체)
                          - `page`: 페이지 번호 (1 부터 시작) - 선택 (기본값: 1)
                          - `size`: 페이지 크기 - 선택 (기본값: 50)
                          - `sort`: 정렬 기준 - 선택 (기본값: distanceFromStation,ASC)

                          #### 카테고리 (category)
                          | 값 | 설명 |
                          |------|------|
                          | `etc` | 문화시설 + 관광지 |
                          | `shopping` | 쇼핑 (쇼핑몰, 시장, 면세점) |
                          | `activity` | 레포츠 (스포츠 시설, 액티비티) |
                          | `restaurant` | 음식점 (카페 제외) |
                          | `cafe` | 카페 (음식점 중 '카페' 키워드 포함) |
                          | *없음* | 모든 카테고리 (기본값) |

                          #### 카테고리 매핑 규칙
                          - **`etc`**: 서버의 `문화시설`, `관광지` 카테고리를 모두 포함
                          - **`shopping`**: 서버의 `쇼핑` 카테고리
                          - **`activity`**: 서버의 `레포츠` 카테고리
                          - **`restaurant`**: 서버의 `음식점` 카테고리에서 '카페' 키워드가 **제외**된 항목
                          - **`cafe`**: 서버의 `음식점` 카테고리에서 이름에 '카페'가 **포함**된 항목
                          - **`없음`** (파라미터 미전송): 모든 카테고리 반환 (기본값)

                          #### 정렬 기준 (sort)
                          | 값 | 설명 | 사용 상황 |
                          |------|------|------|
                          | `distanceFromStation,ASC` | 역에서 가까운 순 (기본값) | 기본 정렬 |
                          | `distanceFromUser,ASC` | 사용자 위치에서 가까운 순 | 사용자 기준 탐색 |
                          | `distanceFromCenter,ASC` | 지도 중심에서 가까운 순 | 지도에서 장소 탐색 시 (mapLat/mapLon 필수) |

                          #### 체류 시간 계산 로직
                          1. **왕복 도보 시간 계산**: 사용자 → 장소 → 역 (왕복)
                          2. **플랫폼 대기 시간**: 10 분 (역 도착 후 열차 탑승까지)
                          3. **체류 가능 시간**: `남은 시간 - 왕복 도보 시간 - 플랫폼 대기 시간`

                          #### 방문 가능 여부 (visitable)
                          - **체류 가능 시간** = `남은 시간 - 왕복 도보 시간 - 플랫폼 대기 시간 (10 분)`
                          - **최소 체류 시간**: 10 분
                          - `stayableMinutes >= 10` → `visitable = true` (방문 가능)
                          - `stayableMinutes < 10` → `visitable = false` (시간 부족)

                          #### 응답 데이터
                          - `content`: 장소 목록 (페이지 크기만큼)
                          - `number`: 현재 페이지 번호 (0-based, API 는 1-based 입력)
                          - `size`: 페이지 크기
                          - `totalElements`: 전체 장소 수
                          - `totalPages`: 전체 페이지 수
                          - `hasNext`: 다음 페이지 존재 여부
                          - `sort`: 정렬 정보
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "방문 가능 장소 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "조회 성공",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "방문 가능 장소 조회가 완료되었습니다.",
                                              "data": {
                                                "number": 0,
                                                "size": 50,
                                                "totalPages": 11,
                                                "hasNext": true,
                                                "sort": {
                                                  "empty": false,
                                                  "sorted": true,
                                                  "unsorted": false
                                                },
                                                "content": [
                                                  {
                                                    "placeId": "2495561",
                                                    "name": "서울로 7017",
                                                    "category": "관광지",
                                                    "address": "서울특별시 중구 한강대로 405",
                                                    "latitude": 37.5545193912,
                                                    "longitude": 126.9706609645,
                                                    "distanceFromUser": 15.5,
                                                    "distanceFromStation": 15.5,
                                                    "walkTimeFromStation": 1,
                                                    "stayableMinutes": 488,
                                                    "visitable": true,
                                                    "imageUrl": "http://tong.visitkorea.or.kr/.../image2_1.jpg"
                                                  },
                                                  {
                                                    "placeId": "3399181",
                                                    "name": "서울역 옥상정원 (The Roof)",
                                                    "category": "관광지",
                                                    "address": "서울특별시 중구 한강대로 405 (봉래동 2 가) 4 층 옥외주차장 왼편",
                                                    "latitude": 37.5545416306,
                                                    "longitude": 126.9717664185,
                                                    "distanceFromUser": 92.8,
                                                    "distanceFromStation": 92.8,
                                                    "walkTimeFromStation": 2,
                                                    "stayableMinutes": 486,
                                                    "visitable": true,
                                                    "imageUrl": "http://tong.visitkorea.or.kr/.../image2_1.jpg"
                                                  }
                                                ],
                                                "totalElements": 541
                                              }
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
                                            name = "역 ID 없음",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "출발 역 ID 는 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "위도/경도 없음",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "사용자 위도/경도는 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "남은 시간 없음",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "열차 출발까지 남은 시간은 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "잘못된 남은 시간",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "남은 시간은 1 분 이상이어야 합니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 역",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "역 없음",
                                    value = """
                                            {
                                              "code": 404,
                                              "message": "역을 찾을 수 없습니다."
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
    ResponseEntity<BaseResponse<Page<AvailablePlace>>> findAvailablePlaces(
            @Parameter(
                    description = "출발 역 ID",
                    required = true,
                    example = "1"
            )
            @NotNull(message = "출발 역 ID 는 필수입니다.")
            Long stationId,

            @Parameter(
                    description = "사용자 현재 위치 위도",
                    required = true,
                    example = "37.5546"
            )
            @NotNull(message = "사용자 위도는 필수입니다.")
            double userLat,

            @Parameter(
                    description = "사용자 현재 위치 경도",
                    required = true,
                    example = "126.9706"
            )
            @NotNull(message = "사용자 경도는 필수입니다.")
            double userLon,

            @Parameter(
                    description = "지도 중심 위도 (distanceFromCenter 정렬 사용 시 필수)",
                    required = false,
                    example = "37.5550"
            )
            Double mapLat,

            @Parameter(
                    description = "지도 중심 경도 (distanceFromCenter 정렬 사용 시 필수)",
                    required = false,
                    example = "126.9710"
            )
            Double mapLon,

            @Parameter(
                    description = "열차 출발까지 남은 시간 (분)",
                    required = true,
                    example = "60"
            )
            @NotNull(message = "열차 출발까지 남은 시간은 필수입니다.")
            @Min(value = 1, message = "남은 시간은 1 분 이상이어야 합니다.")
            int remainingMinutes,

            @Parameter(
                    description = "검색 키워드 (장소명, 카테고리 부분 일치)",
                    required = false,
                    example = "서울로"
            )
            String keyword,

            @Parameter(
                    description = "카테고리 필터 (etc, shopping, activity, restaurant, cafe). 미전송 시 모든 카테고리",
                    required = false,
                    example = "etc"
            )
            String category,

            @Parameter(
                    description = "페이지 번호 (1 부터 시작)",
                    required = false,
                    example = "1"
            )
            @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
            int page,

            @Parameter(
                    description = "페이지 크기",
                    required = false,
                    example = "50"
            )
            @Min(value = 50, message = "페이지 크기는 50 이상이어야 합니다.")
            int size,

            @Parameter(
                    description = "정렬 기준 (형식: 필드명,ASC|DESC) - 예: distanceFromStation,ASC / distanceFromUser,ASC / " +
                                  "distanceFromCenter,ASC",
                    required = false,
                    example = "distanceFromStation,ASC"
            )
            String sort
    );

    @Operation(
            summary = "장소 상세 정보 조회",
            description = """
                          ### 특정 장소의 상세 정보를 조회합니다.

                          #### 경로 변수 (Path Variable)
                          - `/api/v2/places/{placeId}` 형태로 호출

                          #### 요청 파라미터
                          - `placeId`: 장소 ID (VisitKorea contentId) - 필수 (경로 변수)
                          - `stationId`: 출발 역 ID - 필수
                          - `userLat`: 사용자 현재 위치 위도 - 필수
                          - `userLon`: 사용자 현재 위치 경도 - 필수
                          - `remainingMinutes`: 열차 출발까지 남은 시간 (분) - 필수

                          #### 장소 ID (placeId)
                          - VisitKorea API 의 `contentId` 를 그대로 사용
                          - 모든 콘텐츠 타입에서 고유한 숫자 문자열
                          - 예: "2495561" (서울로 7017)

                          #### 제공 정보
                          - **기본 정보**: 이름, 카테고리, 주소, 위경도
                          - **거리 정보**:
                            - `distanceFromStation`: 역에서 장소까지 직선 거리 (미터)
                            - `walkTimeFromStation`: 역에서 장소까지 도보 소요 시간 (분)
                          - **시간 정보**:
                            - `stayableMinutes`: 체류 가능 시간 (분)
                            - `leaveTime`: 역으로 출발해야 하는 시간
                          - **방문 가능 여부**: `visitable`
                            - `stayableMinutes >= 10` → `true` (최소 10 분 이상 체류 가능)
                            - `stayableMinutes < 10` → `false` (시간 부족)
                          - **이미지 목록**: 무제한 (대표 이미지 + VisitKorea API 전체 이미지)
                          - **휴무일**: `restDate`
                          - **이용시간**: `useTime` (타입별로 필드명 상이)
                          - **타입별 특화 정보**:
                            - **관광지 (12)**: 개장일, 이용시기, 체험가능연령, 수용인원, 유모차대여, 애완동물동반
                            - **음식점 (39)**: 대표메뉴, 취급메뉴, 좌석수, 금연/흡연, 포장가능, 어린이놀이방
                            - **문화시설 (14)**: 관람소요시간, 이용요금, 할인정보, 수용인원, 주차시설
                            - **레포츠 (28)**: 개장기간, 입장료, 예약안내, 규모, 체험가능연령
                            - **쇼핑 (38)**: 영업시간, 판매품목, 매장안내, 규모, 장서는날

                          #### 응답 데이터 (타입별 상이)
                          - 모든 타입이 공통 필드를 가지며, 타입별 특화 필드가 추가됩니다.
                          - `category` 필드로 타입을 구분할 수 있습니다.

                          #### 체류 시간 계산
                          ```
                          왕복 도보 시간 = 편도 도보 시간 × 2
                          플랫폼 대기 시간 = 10 분 (고정)
                          체류 가능 시간 = 남은 시간 - 왕복 도보 시간 - 플랫폼 대기 시간
                          ```
                          """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "장소 상세 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PlaceDetail.class),
                            examples = {
                                    @ExampleObject(
                                            name = "관광지 상세 정보",
                                            value = """
                                                    {
                                                      "code": 200,
                                                      "message": "장소 상세 정보 조회가 완료되었습니다.",
                                                      "data": {
                                                        "placeId": "2495561",
                                                        "name": "서울로 7017",
                                                        "category": "관광지",
                                                        "address": "서울특별시 중구 한강대로 405",
                                                        "latitude": 37.5545193912,
                                                        "longitude": 126.9706609645,
                                                        "distanceFromStation": 15,
                                                        "walkTimeFromStation": 1,
                                                        "stayableMinutes": 488,
                                                        "visitable": true,
                                                        "stationLat": 37.554648,
                                                        "stationLon": 126.97073,
                                                        "leaveTime": "2026-03-30T11:50:00",
                                                        "images": [
                                                          "http://tong.visitkorea.or.kr/.../image2_1.jpg",
                                                          "http://tong.visitkorea.or.kr/.../image3_1.jpg"
                                                        ],
                                                        "phoneNumber": "02-1234-5678",
                                                        "restDate": "연중무휴",
                                                        "useTime": "24 시간",
                                                        "openDate": "2017 년 09 월 01 일",
                                                        "useSeason": "연중",
                                                        "expAgeRange": "전연령",
                                                        "expGuide": "가이드 투어 가능",
                                                        "heritage1": "없음",
                                                        "accomCount": "5000 명",
                                                        "chkBabyCarriage": "대여가능",
                                                        "chkPet": "불가능"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "음식점 상세 정보",
                                            value = """
                                                    {
                                                      "code": 200,
                                                      "message": "장소 상세 정보 조회가 완료되었습니다.",
                                                      "data": {
                                                        "placeId": "2914090",
                                                        "name": "유즈라멘",
                                                        "category": "음식점",
                                                        "address": "서울특별시 중구 만리재로 217 경김회관",
                                                        "latitude": 37.5569496205,
                                                        "longitude": 126.9682222291,
                                                        "distanceFromStation": 339,
                                                        "walkTimeFromStation": 7,
                                                        "stayableMinutes": 476,
                                                        "visitable": true,
                                                        "stationLat": 37.554648,
                                                        "stationLon": 126.97073,
                                                        "leaveTime": "2026-03-30T11:50:00",
                                                        "images": [
                                                          "http://tong.visitkorea.or.kr/.../image2_1.jpg"
                                                        ],
                                                        "phoneNumber": "02-1234-5678",
                                                        "restDate": "일요일 휴무",
                                                        "useTime": "AM 11:00 ~ PM 10:00",
                                                        "firstMenu": "유즈라멘",
                                                        "treatMenu": "라멘, 돈코츠라멘",
                                                        "seat": "30 석",
                                                        "smoking": "금연",
                                                        "packing": "가능",
                                                        "kidsFacility": "없음",
                                                        "openDateFood": "2015 년 03 월 01 일"
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 필수 파라미터 누락 또는 유효성 검사 실패",
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
                                                      "message": "출발 역 ID 는 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "위도/경도 없음",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "사용자 위도/경도는 필수입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "남은 시간 없음",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "열차 출발까지 남은 시간은 필수입니다."
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
    ResponseEntity<BaseResponse<PlaceDetail>> getPlaceDetail(
            @Parameter(
                    description = "장소 ID (VisitKorea contentId)",
                    required = true,
                    example = "2495561"
            )
            @PathVariable String placeId,

            @Parameter(
                    description = "출발 역 ID",
                    required = true,
                    example = "1"
            )
            @NotNull(message = "출발 역 ID 는 필수입니다.")
            Long stationId,

            @Parameter(
                    description = "사용자 현재 위치 위도",
                    required = true,
                    example = "37.5546"
            )
            @NotNull(message = "사용자 위도는 필수입니다.")
            double userLat,

            @Parameter(
                    description = "사용자 현재 위치 경도",
                    required = true,
                    example = "126.9706"
            )
            @NotNull(message = "사용자 경도는 필수입니다.")
            double userLon,

            @Parameter(
                    description = "열차 출발까지 남은 시간 (분)",
                    required = true,
                    example = "60"
            )
            @NotNull(message = "열차 출발까지 남은 시간은 필수입니다.")
            @Min(value = 1, message = "남은 시간은 1 분 이상이어야 합니다.")
            int remainingMinutes
    );

}
