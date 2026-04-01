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
                      
                      ### 📌 개요
                      VisitKorea(한국관광공사) 공공데이터를 기반으로, **역에서 방문 가능한 장소 정보**를 제공합니다.
                      
                      **실시간으로 방문 가능 여부를 계산**하여, 사용자가 현재 시간과 열차 출발 시간을 고려했을 때 실제로 방문할 수 있는 장소만 필터링합니다.
                      
                      ### 🎯 주요 기능
                      | API | 설명 |
                      |-----|------|
                      | `GET /api/v2/places` | 역 주변 방문 가능 장소 목록 조회 (페이징, 필터링, 정렬 지원) |
                      | `GET /api/v2/places/{placeId}` | 특정 장소 상세 정보 조회 (타입별 특화 정보 제공) |
                      
                      ### 인증 방식
                      - **비인증 가능**: 모든 API 는 인증 없이 이용 가능합니다.
                      
                      ### 데이터 소스
                      | 소스 | 저장 정보 | 갱신 주기 |
                      |------|----------|----------|
                      | **내부 DB** | 역 정보, 장소 위치 (위경도), 카테고리 | 수시 |
                      | **VisitKorea API** | 장소 상세 정보, 이미지, 휴무일, 이용시간 | 실시간 (캐시 미스 시) |
                      | **Redis GEO** | 역 기준 반경 3km 이내 장소 | 24 시간 |
                      
                      ### 캐싱 전략
                      ```
                      ┌─────────────────────────────────────────────────────────┐
                      │ 1. Redis GEO 검색 (역 기준 반경 3km)                      │
                      │    ↓ (데이터 없음)                                       │
                      │ 2. VisitKorea API 호출 → Redis 동기화                    │
                      │    ↓                                                     │
                      │ 3. PlaceCardCache 조회 (24 시간 TTL)                      │
                      │    ↓ (상세 정보 필요)                                    │
                      │ 4. PlaceDetailCache 조회 (7 일 TTL)                        │
                      └─────────────────────────────────────────────────────────┘
                      ```
                      
                      ### 체류 시간 계산 로직
                      ```
                      1. 왕복 도보 시간 = (사용자 → 장소 → 역) × 2
                      2. 플랫폼 대기 시간 = 10 분 (고정)
                      3. 체류 가능 시간 = 남은 시간 - 왕복 도보 시간 - 플랫폼 대기 시간
                      4. 최소 체류 시간 = 10 분
                      ```
                      
                      ### 방문 가능 여부 (visitable) 판정
                      | 시간 기반 visitable | 영업 상태 | **최종 visitable** | 설명 |
                      |---------------------|-----------|-------------------|------|
                      | `false` | 영업 중 | `false` | 시간 부족 |
                      | `false` | 영업 종료 | `false` | 시간 부족 + 영업 안 함 |
                      | `true` | 영업 중 | `true` | 방문 가능 ✅ |
                      | `true` | 영업 종료 | `false` | 시간은 충분하지만 영업 안 함 |
                      
                      ### 클라이언트 사용 가이드
                      1. **목록 조회**: `GET /api/v2/places?stationId=1&userLat=37.5&userLon=126.9&remainingMinutes=60`
                      2. **필터링**: `category=etc` (문화시설 + 관광지), `category=cafe` (카페만)
                      3. **정렬**: `sort=distanceFromUser,ASC` (사용자 기준 가까운 순)
                      4. **상세 조회**: `GET /api/v2/places/{placeId}` → 타입별 특화 정보 확인
                      """
)
public interface PlaceApiDocsV2 {

    @Operation(
            summary = "방문 가능 장소 목록 조회",
            description = """
                          ### 사용자 위치와 역, 남은 시간을 입력받아 **방문 가능한 장소 목록**을 조회합니다.
                          
                          #### 📋 요청 파라미터
                          | 파라미터 | 타입 | 필수 | 기본값 | 설명 |
                          |----------|------|------|--------|------|
                          | `stationId` | Long | ✅ | - | 출발 역 ID |
                          | `userLat` | double | ✅ | - | 사용자 현재 위치 위도 |
                          | `userLon` | double | ✅ | - | 사용자 현재 위치 경도 |
                          | `remainingMinutes` | int | ✅ | - | 열차 출발까지 남은 시간 (분) |
                          | `mapLat` | Double | ❌ | null | 지도 중심 위도 (`distanceFromCenter` 정렬 사용 시 필수) |
                          | `mapLon` | Double | ❌ | null | 지도 중심 경도 (`distanceFromCenter` 정렬 사용 시 필수) |
                          | `keyword` | String | ❌ | `""` | 검색 키워드 (장소명, 카테고리 부분 일치) |
                          | `category` | String | ❌ | `"전체"` | 카테고리 필터 (아래 표 참조) |
                          | `page` | int | ❌ | `1` | 페이지 번호 (1 부터 시작) |
                          | `size` | int | ❌ | `50` | 페이지 크기 (최소 50) |
                          | `sort` | String | ❌ | `"distanceFromStation,ASC"` | 정렬 기준 (필드명,ASC|DESC) |
                          
                          #### 🏷️ 카테고리 (category)
                          | 값 | 설명 | 포함 서버 카테고리 |
                          |------|------|-------------------|
                          | `전체` | 모든 카테고리 | 전체 |
                          | `etc` | 문화시설 + 관광지 | 문화시설, 관광지 |
                          | `shopping` | 쇼핑 | 쇼핑 |
                          | `activity` | 레포츠 | 레포츠 |
                          | `restaurant` | 음식점 (카페 제외) | 음식점 (카페 키워드 제외) |
                          | `cafe` | 카페 | 음식점 중 '카페' 키워드 포함 |
                          
                          #### 📊 정렬 기준 (sort)
                          | 값 | 설명 | 사용 상황 |
                          |------|------|------|
                          | `distanceFromStation,ASC` | 역에서 가까운 순 (기본값) | 기본 정렬 |
                          | `distanceFromUser,ASC` | 사용자 위치에서 가까운 순 | 사용자 기준 탐색 |
                          | `distanceFromCenter,ASC` | 지도 중심에서 가까운 순 | 지도에서 장소 탐색 시 (`mapLat`/`mapLon` 필수) |
                          
                          #### ⏱️ 체류 시간 계산 로직
                          ```
                          1. 왕복 도보 시간 = (사용자 → 장소 → 역) × 2
                          2. 플랫폼 대기 시간 = 10 분 (고정, 역 도착 후 열차 탑승까지 대기)
                          3. 체류 가능 시간 = 남은 시간 - 왕복 도보 시간 - 플랫폼 대기 시간
                          4. 최소 체류 시간 = 10 분
                          ```
                          
                          #### ✅ 방문 가능 여부 (visitable) 판정
                          **`visitable` 은 다음 두 조건의 조합으로 결정됩니다:**
                          
                          1. **시간 기반 visitable**: `stayableMinutes >= 10`
                          2. **영업 상태**: `googleOpeningStatus == "영업 중"`
                          
                          | 시간 기반 | 영업 상태 | **최종 visitable** | 설명 |
                          |-----------|-----------|-------------------|------|
                          | `false` | 영업 중 | `false` | 시간 부족 |
                          | `false` | 영업 종료 | `false` | 시간 부족 + 영업 안 함 |
                          | `true` | 영업 중 | `true` | 방문 가능 ✅ |
                          | `true` | 영업 종료 | `false` | 시간은 충분하지만 영업 안 함 |
                          
                          #### 📦 응답 데이터
                          ```json
                          {
                            "code": 200,
                            "message": "방문 가능 장소 조회가 완료되었습니다.",
                            "data": {
                              "number": 0,          // 현재 페이지 번호 (0-based)
                              "size": 50,           // 페이지 크기
                              "totalPages": 11,     // 전체 페이지 수
                              "hasNext": true,      // 다음 페이지 존재 여부
                              "content": [          // 장소 목록
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
                                  "imageUrl": "http://..."
                                }
                              ],
                              "totalElements": 541
                            }
                          }
                          ```
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
                          ### 특정 장소의 **상세 정보**를 조회합니다.
                          
                          #### 📋 경로 변수
                          | 변수 | 타입 | 필수 | 설명 |
                          |------|------|------|------|
                          | `placeId` | String | ✅ | 장소 ID (VisitKorea contentId) |
                          
                          #### 📋 요청 파라미터
                          | 파라미터 | 타입 | 필수 | 설명 |
                          |----------|------|------|------|
                          | `stationId` | Long | ✅ | 출발 역 ID |
                          | `userLat` | double | ✅ | 사용자 현재 위치 위도 |
                          | `userLon` | double | ✅ | 사용자 현재 위치 경도 |
                          | `remainingMinutes` | int | ✅ | 열차 출발까지 남은 시간 (분) |
                          
                          #### 🏷️ 장소 ID (placeId)
                          - VisitKorea API 의 `contentId` 를 그대로 사용합니다.
                          - 모든 콘텐츠 타입에서 고유한 숫자 문자열입니다.
                          - 예: `"2495561"` (서울로 7017)
                          
                          #### 📦 제공 정보
                          | 정보 | 설명 |
                          |------|------|
                          | **기본 정보** | 이름, 카테고리, 주소, 위경도 |
                          | **거리 정보** | `distanceFromStation` (역에서 장소까지 직선 거리, 미터), `walkTimeFromStation` (역에서 장소까지 도보 소요 시간, 분) |
                          | **시간 정보** | `stayableMinutes` (체류 가능 시간, 분), `leaveTime` (역으로 출발해야 하는 시간) |
                          | **방문 가능 여부** | `visitable` (시간 기반 + 영업 상태 조합) |
                          | **Google Places 정보** | `googleWeekdayDescriptions` (요일별 운영 시간), `isOpened` (실시간 영업 상태), `googleOpeningStatus` (문자열) |
                          | **이미지 목록** | 무제한 (대표 이미지 + VisitKorea API 전체 이미지) |
                          | **휴무일** | `restDate` |
                          | **이용시간** | `useTime` |
                          | **타입별 특화 정보** | 아래 표 참조 |
                          
                          #### 🎯 타입별 특화 정보
                          | 타입 | 코드 | 특화 필드 |
                          |------|------|----------|
                          | **관광지** | `TOURIST` | `openDate`, `useSeason`, `expAgeRange`, `expGuide`, `heritage1`, `accomCount`, `chkBabyCarriage`, `chkPet` |
                          | **음식점** | `RESTAURANT` | `firstMenu`, `treatMenu`, `seat`, `smoking`, `packing`, `kidsFacility`, `openDateFood` |
                          | **문화시설** | `CULTURE` | `spendTime`, `useFee`, `discountInfo`, `accomCountCulture`, `parkingCulture` |
                          | **레포츠** | `SPORTS` | `openPeriod`, `useFeeLeports`, `reservation`, `scaleLeports`, `expAgeRangeLeports` |
                          | **쇼핑** | `SHOPPING` | `openTime`, `saleItem`, `shopGuide`, `scaleShopping`, `fairDay` |
                          
                          #### ⏱️ 체류 시간 계산
                          ```
                          왕복 도보 시간 = 편도 도보 시간 × 2
                          플랫폼 대기 시간 = 10 분 (고정)
                          체류 가능 시간 = 남은 시간 - 왕복 도보 시간 - 플랫폼 대기 시간
                          ```
                          
                          #### 🕐 역으로 출발해야 하는 시간 (leaveTime)
                          ```
                          leaveTime = 현재 시간 + (남은 시간 - 편도 도보 시간)
                          ```
                          
                          **의미**: 사용자가 현재 장소에 있을 때, **최소 이 시간에는 역으로 출발**해야 합니다.
                          
                          **포함 시간**: 도보 이동 시간 + 플랫폼 대기 시간 (10 분)
                          
                          **예시**:
                          - 현재 시간: `14:00`
                          - 남은 시간: `60 분`
                          - 편도 도보 시간: `10 분`
                          - **leaveTime**: `14:00 + (60 - 10) = 14:50`
                          - 해석: *"14:50 에 역으로 출발하면, 도보 10 분 + 플랫폼 대기 10 분 후 열차 탑승 가능"*
                          
                          #### 📦 응답 데이터 (관광지 예시)
                          ```json
                          {
                            "code": 200,
                            "message": "장소 상세 정보 조회가 완료되었습니다.",
                            "data": {
                              "placeType": "TOURIST",
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
                              "stationLatitude": 37.554648,
                              "stationLongitude": 126.97073,
                              "leaveTime": "2026-03-30T11:50:00",
                              "images": [
                                "http://tong.visitkorea.or.kr/.../image2_1.jpg",
                                "http://tong.visitkorea.or.kr/.../image3_1.jpg"
                              ],
                              "phoneNumber": "02-1234-5678",
                              "restDate": "연중무휴",
                              "useTime": "24 시간",
                              "googleWeekdayDescriptions": ["월요일: 오전 9:00 ~ 오후 6:00"],
                              "isOpened": true,
                              "googleOpeningStatus": "영업 중",
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
