package com.timespot.backend.domain.place.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.annotation.CustomPageResponse;
import com.timespot.backend.domain.place.dto.PlaceResponseDtoV2.AvailablePlace;
import com.timespot.backend.domain.place.dto.PlaceResponseDtoV2.PlaceDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

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
 */
@Tag(name = "Place V2", description = "장소 API V2 (VisitKorea 공공데이터 기반)")
public interface PlaceApiDocsV2 {

    @Operation(
            summary = "방문 가능 장소 목록 조회",
            description = """
                          사용자 위치와 역, 남은 시간을 입력받아 방문 가능한 장소 목록을 조회합니다.
                          
                          ## 체류 시간 계산 로직
                          1. 사용자 위치 → 장소 위치 → 역 위치 (왕복)
                          2. 왕복 도보 시간을 제외한 나머지 시간을 체류 가능 시간으로 계산
                          3. 플랫폼 대기 시간 (10 분) 고려
                          
                          ## 방문 가능 여부 (visitable)
                          - 체류 가능 시간이 0 분 이상이면 `true`
                          - 체류 가능 시간이 음수이면 `false` (시간 부족으로 방문 불가)
                          - 시간이 지나도 장소 목록은 유지되며, visitable 필드로 상태 확인 가능
                          
                          ## 필터링
                          - keyword: 장소명 또는 카테고리에서 검색
                          - category: 카테고리 필터 (전체, 관광지, 문화시설, 레포츠, 쇼핑, 음식점)
                          
                          ## 정렬 (sort 파라미터)
                          - distanceFromStation,ASC: 역에서 가까운 순 (기본값)
                          - distanceFromUser,ASC: 사용자 위치에서 가까운 순
                          - distanceFromCenter,ASC: 지도 중심에서 가까운 순 (mapLat/mapLon 제공 시)
                          
                          ## 지도 중심 (mapLat, mapLon)
                          - 제공되지 않으면: 역 중심 정렬
                          - 제공되면: 해당 위치를 기준으로 distanceFromCenter 계산 후 정렬
                          """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "방문 가능 장소 조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 역",
                    content = @Content
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
            @Parameter(description = "출발 역 ID", required = true, example = "1")
            @NotNull(message = "출발 역 ID 는 필수입니다.")
            Long stationId,

            @Parameter(description = "사용자 위도", required = true, example = "37.5546")
            @NotNull(message = "사용자 위도는 필수입니다.")
            double userLat,

            @Parameter(description = "사용자 경도", required = true, example = "126.9706")
            @NotNull(message = "사용자 경도는 필수입니다.")
            double userLon,

            @Parameter(description = "지도 중심 위도", required = false, example = "37.5550")
            Double mapLat,

            @Parameter(description = "지도 중심 경도", required = false, example = "126.9710")
            Double mapLon,

            @Parameter(description = "열차 출발까지 남은 시간 (분)", required = true, example = "60")
            @NotNull(message = "열차 출발까지 남은 시간은 필수입니다.")
            @Min(value = 1, message = "남은 시간은 1 분 이상이어야 합니다.")
            int remainingMinutes,

            @Parameter(description = "검색 키워드", required = false, example = "카페")
            String keyword,

            @Parameter(description = "카테고리", required = false, example = "전체")
            String category,

            @Parameter(description = "페이지 번호", required = false, example = "1")
            @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
            int page,

            @Parameter(description = "페이지 크기", required = false, example = "20")
            @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
            int size,

            @Parameter(description = "정렬 기준", required = false, example = "distanceFromStation,ASC")
            String sort
    );

    @Operation(
            summary = "장소 상세 정보 조회",
            description = """
                          특정 장소의 상세 정보를 조회합니다.
                          
                          ## 장소 ID (placeId)
                          - VisitKorea API 의 contentId 를 그대로 사용 (숫자만)
                          - 예: "126644"
                          - 모든 콘텐츠 타입에서 고유한 값
                          
                          ## 제공 정보
                          - 장소 기본 정보 (이름, 카테고리, 주소, 좌표)
                          - 역 기준 거리 및 도보 소요 시간
                          - 체류 가능 시간 (stayableMinutes) 및 방문 가능 여부 (visitable)
                          - 출발 시간 (leaveTime)
                          - 이미지 목록
                          - 휴무일, 이용시간
                          - 타입별 특화 정보 (관광지: 개장일/이용시기, 음식점: 대표메뉴/좌석수 등)
                          
                          ## 방문 가능 여부 (visitable)
                          - 체류 가능 시간이 0 분 이상이면 `true`
                          - 체류 가능 시간이 음수이면 `false` (시간 부족으로 방문 불가)
                          - 시간이 지나 visitable 이 `false` 가 되더라도 장소 정보는 유지됩니다
                          """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "장소 상세 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = PlaceDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 장소",
                    content = @Content
            )
    })
    ResponseEntity<BaseResponse<PlaceDetail>> getPlaceDetail(
            @Parameter(description = "장소 ID", required = true, example = "126644")
            @NotBlank(message = "장소 ID 는 필수입니다.")
            String placeId,

            @Parameter(description = "출발 역 ID", required = true, example = "1")
            @NotNull(message = "출발 역 ID 는 필수입니다.")
            Long stationId,

            @Parameter(description = "사용자 위도", required = true, example = "37.5546")
            @NotNull(message = "사용자 위도는 필수입니다.")
            double userLat,

            @Parameter(description = "사용자 경도", required = true, example = "126.9706")
            @NotNull(message = "사용자 경도는 필수입니다.")
            double userLon,

            @Parameter(description = "열차 출발까지 남은 시간 (분)", required = true, example = "60")
            @NotNull(message = "열차 출발까지 남은 시간은 필수입니다.")
            @Min(value = 1, message = "남은 시간은 1 분 이상이어야 합니다.")
            int remainingMinutes
    );

}
