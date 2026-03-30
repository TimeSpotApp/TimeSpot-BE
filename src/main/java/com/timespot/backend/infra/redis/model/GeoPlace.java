package com.timespot.backend.infra.redis.model;

import lombok.Builder;
import lombok.Getter;

/**
 * PackageName : com.timespot.backend.infra.redis.model
 * FileName    : GeoPlace
 * Author      : loadingKKamo21
 * Date        : 26. 3. 29.
 * Description : Redis GEO 장소 정보 모델
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 29.    loadingKKamo21       Initial creation
 */
@Getter
@Builder
public class GeoPlace {

    /**
     * 장소 고유 식별자
     */
    private final String placeId;

    /**
     * 위도 (latitude)
     */
    private final Double latitude;

    /**
     * 경도 (longitude)
     */
    private final Double longitude;

    /**
     * 기준점으로부터의 거리 (미터)
     */
    private final Double distance;

}
