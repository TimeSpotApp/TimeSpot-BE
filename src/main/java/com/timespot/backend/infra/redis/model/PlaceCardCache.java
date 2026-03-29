package com.timespot.backend.infra.redis.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.timespot.backend.infra.redis.model
 * FileName    : PlaceCardCache
 * Author      : loadingKKamo21
 * Date        : 26. 3. 29.
 * Description : 장소 카드 정보 캐시 모델 (지도 마커용 간단 정보)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 29.    loadingKKamo21       Initial creation
 */
@Getter
@RequiredArgsConstructor
public class PlaceCardCache {

    private final String placeId;   // 장소 고유 식별자
    private final String name;      // 장소명
    private final String category;  // 카테고리
    private final String address;   // 전체 주소
    private final Double latitude;  // 위도
    private final Double longitude; // 경도
    private final Double distance;  // 거리 (미터)
    private final String imageUrl;  // 대표 이미지 URL

}
