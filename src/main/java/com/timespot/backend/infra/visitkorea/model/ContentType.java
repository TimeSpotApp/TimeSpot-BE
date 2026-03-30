package com.timespot.backend.infra.visitkorea.model;

import static com.timespot.backend.common.response.ErrorCode.PLACE_API_CONTENT_TYPE_NOT_SUPPORTED;
import static lombok.AccessLevel.PRIVATE;

import com.timespot.backend.common.error.GlobalException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.timespot.backend.infra.visitkorea.model
 * FileName    : ContentType
 * Author      : loadingKKamo21
 * Date        : 26. 3. 29.
 * Description : 콘텐츠 타입 열거형 (12: 관광지, 14: 문화시설, 28: 레포츠, 38: 쇼핑, 39: 음식점)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 29.    loadingKKamo21       Initial creation
 */
@Getter
@RequiredArgsConstructor(access = PRIVATE)
public enum ContentType {

    CONTENT_TYPE_TOURIST("12", "관광명소"),
    CONTENT_TYPE_CULTURE("14", "문화시설"),
    CONTENT_TYPE_SPORTS("28", "레포츠"),
    CONTENT_TYPE_SHOPPING("38", "쇼핑"),
    CONTENT_TYPE_RESTAURANT("39", "음식점");

    private static final Map<String, ContentType> CONTENT_TYPE_MAP = Stream.of(values())
                                                                           .collect(Collectors.toUnmodifiableMap(
                                                                                   ContentType::getContentTypeId,
                                                                                   Function.identity()
                                                                           ));

    private final String contentTypeId;
    private final String description;

    /**
     * 콘텐츠 타입 조회
     *
     * @param contentTypeId 콘텐츠 타입 ID
     * @return 콘텐츠 타입 enum
     */
    public static ContentType from(final String contentTypeId) {
        return Optional.ofNullable(contentTypeId)
                       .map(CONTENT_TYPE_MAP::get)
                       .orElseThrow(() -> new GlobalException(PLACE_API_CONTENT_TYPE_NOT_SUPPORTED));
    }

    /**
     * 전체 콘텐츠 타입 목록 조회 (API 호출용)
     *
     * @return 콘텐츠 타입 목록
     */
    public static List<ContentType> getAllContentTypes() {
        return List.of(
                CONTENT_TYPE_TOURIST,
                CONTENT_TYPE_CULTURE,
                CONTENT_TYPE_SPORTS,
                CONTENT_TYPE_SHOPPING,
                CONTENT_TYPE_RESTAURANT
        );
    }

}
