package com.timespot.backend.domain.place.service;

import com.timespot.backend.domain.place.dto.PlaceResponseDtoV2.AvailablePlace;
import com.timespot.backend.domain.place.dto.PlaceResponseDtoV2.PlaceDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * PackageName : com.timespot.backend.domain.place.service
 * FileName    : PlaceServiceV2
 * Author      : loadingKKamo21
 * Date        : 26. 3. 29.
 * Description : Place API V2 서비스 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 29.    loadingKKamo21       Initial creation
 */
public interface PlaceServiceV2 {

    /**
     * 역 주변 방문 가능 장소 조회
     * - 사용자 위치 → 장소 → 역 경로를 고려하여 체류 가능 시간 계산
     *
     * @param stationId        출발 역 ID
     * @param userLat          사용자 위도
     * @param userLon          사용자 경도
     * @param mapLat           지도 중심 위도 (선택, null 이면 역 중심)
     * @param mapLon           지도 중심 경도 (선택, null 이면 역 중심)
     * @param remainingMinutes 열차 출발까지 남은 시간 (분)
     * @param keyword          검색 키워드 (선택)
     * @param category         카테고리 (전체, 관광지, 문화시설, 레포츠, 쇼핑, 음식점)
     * @param pageable         페이지네이션 (Sort 포함)
     * @return 방문 가능 장소 페이지
     */
    Page<AvailablePlace> findAvailablePlaces(
            Long stationId,
            double userLat,
            double userLon,
            Double mapLat,
            Double mapLon,
            int remainingMinutes,
            String keyword,
            String category,
            Pageable pageable
    );

    /**
     * 장소 상세 정보 조회
     * - 체류 가능 시간 및 출발 시간 계산
     *
     * @param placeId          장소 ID
     * @param stationId        출발 역 ID
     * @param userLat          사용자 위도
     * @param userLon          사용자 경도
     * @param remainingMinutes 열차 출발까지 남은 시간 (분)
     * @return 장소 상세 정보
     */
    PlaceDetail getPlaceDetail(
            String placeId,
            Long stationId,
            double userLat,
            double userLon,
            int remainingMinutes
    );

}
