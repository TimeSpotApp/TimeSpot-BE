package com.timespot.backend.infra.redis.model;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.timespot.backend.infra.redis.model
 * FileName    : PlaceDetailCache
 * Author      : loadingKKamo21
 * Date        : 26. 3. 29.
 * Description: 장소 상세 정보 캐시 모델 (이미지, 전화번호, 휴무일, 주차시설 등)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 29.    loadingKKamo21       Initial creation
 */
@Getter
@RequiredArgsConstructor
public class PlaceDetailCache {

    private final String       placeId;         // 장소 고유 식별자
    private final String       contentId;       // VisitKorea 콘텐츠 ID
    private final String       contentTypeId;   // 콘텐츠 타입 ID (12, 14, 28, 38, 39)
    private final String       phoneNumber;     // 전화번호 (문의처)
    private final List<String> images;          // 이미지 목록
    private final String       restDate;        // 휴무일
    private final String       parking;         // 주차시설
    private final String       useTime;         // 이용시간/영업시간

    /**
     * 빈 캐시 객체 생성 (상세 정보 없음)
     */
    public static PlaceDetailCache empty() {
        return new PlaceDetailCache(null, null, null, null, null, null, null, null);
    }

    /**
     * 캐시 정보 비어있는지 확인
     */
    public boolean isEmpty() {
        return placeId == null || placeId.isBlank();
    }

}
