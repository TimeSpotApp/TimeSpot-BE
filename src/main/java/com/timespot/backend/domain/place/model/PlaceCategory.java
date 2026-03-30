package com.timespot.backend.domain.place.model;

import static com.timespot.backend.common.response.ErrorCode.PLACE_CATEGORY_TYPE_NOT_SUPPORTED;
import static lombok.AccessLevel.PRIVATE;

import com.timespot.backend.common.error.GlobalException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.place.model
 * FileName    : PlaceCategory
 * Author      : loadingKKamo21
 * Date        : 26. 3. 30.
 * Description : 장소 카테고리 유형 열거형 (클라이언트 API 용)
 * <p>
 * 클라이언트 요청 카테고리 (etc, shopping, activity, restaurant, cafe) 를
 * 서버 내부 카테고리 (관광지, 문화시설, 레포츠, 쇼핑, 음식점) 로 매핑합니다.
 * </p>
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 30.    loadingKKamo21       Initial creation
 */
@Getter
@RequiredArgsConstructor(access = PRIVATE)
public enum PlaceCategory {

    /**
     * 기타: 문화시설 + 관광지를 포함
     */
    ETC(Set.of("문화시설", "관광지")),

    /**
     * 쇼핑: 쇼핑 카테고리
     */
    SHOPPING(Set.of("쇼핑")),

    /**
     * 액티비티: 레포츠 카테고리
     */
    ACTIVITY(Set.of("레포츠")),

    /**
     * 음식점: 음식점 카테고리 (카페 제외)
     */
    RESTAURANT(Set.of("음식점")),

    /**
     * 카페: 음식점 카테고리 내 '카페' 키워드 필터링
     */
    CAFE(Set.of("카페"));

    private static final Map<String, PlaceCategory> CLIENT_CATEGORY_MAP = Stream.of(values())
                                                                                .collect(Collectors.toUnmodifiableMap(
                                                                                        Enum::name, Function.identity()
                                                                                ));

    /**
     * 서버 내부 카테고리 목록 (관광지, 문화시설, 레포츠, 쇼핑, 음식점)
     */
    private final Set<String> serverCategories;

    /**
     * 클라이언트 요청 카테고리를 서버 내부 카테고리로 변환
     * <p>
     * - ETC → ["문화시설", "관광지"]<br>
     * - SHOPPING → ["쇼핑"]<br>
     * - ACTIVITY → ["레포츠"]<br>
     * - RESTAURANT → ["음식점"] (카페 제외 필터링 필요)<br>
     * - CAFE → ["카페"] (음식점 내 검색어 필터링)
     * </p>
     *
     * @param clientCategory 클라이언트 요청 카테고리 (대소문자 무시)
     * @return PlaceCategory enum
     * @throws GlobalException (PLACE_CATEGORY_TYPE_NOT_SUPPORTED) 지원되지 않는 카테고리
     */
    public static PlaceCategory from(final String clientCategory) {
        return Optional.ofNullable(clientCategory)
                       .map(String::toUpperCase)
                       .map(CLIENT_CATEGORY_MAP::get)
                       .orElseThrow(() -> new GlobalException(PLACE_CATEGORY_TYPE_NOT_SUPPORTED));
    }

    /**
     * 이 카테고리가 장소 이름/카테고리에 '카페' 키워드 필터링이 필요한지 확인
     *
     * @return CAFE 또는 RESTAURANT 인 경우 true
     */
    public boolean requiresCafeKeywordFilter() {
        return this == CAFE || this == RESTAURANT;
    }

    /**
     * 이 카테고리가 '카페' 키워드를 포함하는 장소만 필터링하는지 확인
     *
     * @return CAFE 인 경우 true
     */
    public boolean isCafeOnly() {
        return this == CAFE;
    }

}
