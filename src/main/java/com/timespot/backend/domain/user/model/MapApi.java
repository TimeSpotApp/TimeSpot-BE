package com.timespot.backend.domain.user.model;

import static com.timespot.backend.common.response.ErrorCode.USER_MAP_API_NOT_SUPPORTED;

import com.timespot.backend.common.error.GlobalException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.user.model
 * FileName    : MapApi
 * Author      : loadingKKamo21
 * Date        : 26. 3. 21.
 * Description : 지도 API 유형 열거형 (APPLE, GOOGLE, NAVER)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 21.    loadingKKamo21       Initial creation
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MapApi {

    APPLE("애플", null),
    GOOGLE("구글", "comgooglemaps"),
    NAVER("네이버", "nmap");

    private static final Map<String, MapApi> MAP_API_MAP = Stream.of(values())
                                                                 .collect(Collectors.toUnmodifiableMap(
                                                                         Enum::name, Function.identity())
                                                                 );

    private final String name;
    private final String urlScheme;

    /**
     * 지도 API 유형 조회
     *
     * @param mapApi 지도 API 유형 문자열
     * @return 지도 API 유형 enum
     */
    public static MapApi from(final String mapApi) {
        return Optional.ofNullable(mapApi)
                       .map(String::toUpperCase)
                       .map(MAP_API_MAP::get)
                       .orElseThrow(() -> new GlobalException(USER_MAP_API_NOT_SUPPORTED));
    }

}
