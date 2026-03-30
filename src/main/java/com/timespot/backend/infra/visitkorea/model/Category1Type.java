package com.timespot.backend.infra.visitkorea.model;

import static com.timespot.backend.common.response.ErrorCode.PLACE_API_CATEGORY1_TYPE_NOT_SUPPORTED;
import static lombok.AccessLevel.PRIVATE;

import com.timespot.backend.common.error.GlobalException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.timespot.backend.infra.visitkorea.model
 * FileName    : Category1Type
 * Author      : loadingKKamo21
 * Date        : 26. 3. 29.
 * Description : 카테고리1 타입 열거형 (관광지, 문화시설, 레포츠, 음식점, 쇼핑시설)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 29.    loadingKKamo21       Initial creation
 */
@Getter
@RequiredArgsConstructor(access = PRIVATE)
public enum Category1Type {

    CAT1_NATURE("A01", "자연관광"),
    CAT1_CULTURE("A02", "문화관광"),
    CAT1_SPORTS("A03", "레포츠관광"),
    CAT1_RESTAURANT("A05", "음식점"),
    CAT1_SHOPPING("B02", "쇼핑시설");

    private static final Map<String, Category1Type> CATEGORY1_TYPE_MAP = Stream.of(values())
                                                                               .collect(Collectors.toUnmodifiableMap(
                                                                                       Category1Type::getCat1,
                                                                                       Function.identity()
                                                                               ));

    private final String cat1;
    private final String description;

    /**
     * 카테고리1 타입 조회
     *
     * @param cat1 카테고리1 타입 문자열
     * @return 카테고리1 타입 enum
     */
    public static Category1Type from(final String cat1) {
        return Optional.ofNullable(cat1)
                       .map(String::toUpperCase)
                       .map(CATEGORY1_TYPE_MAP::get)
                       .orElseThrow(() -> new GlobalException(PLACE_API_CATEGORY1_TYPE_NOT_SUPPORTED));
    }

}
