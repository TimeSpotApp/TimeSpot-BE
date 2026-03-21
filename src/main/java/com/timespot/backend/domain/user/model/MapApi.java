package com.timespot.backend.domain.user.model;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.response.ErrorCode;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * PackageName : com.timespot.backend.domain.user.model
 * FileName    : MapApi
 * Author      : loadingKKamo21
 * Date        : 26. 3. 21.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 21.    loadingKKamo21       Initial creation
 */
public enum MapApi {

    APPLE, GOOGLE, NAVER;

    private static final Map<String, MapApi> MAP_API_MAP = Stream.of(values())
                                                                 .collect(Collectors.toUnmodifiableMap(
                                                                         Enum::name, Function.identity())
                                                                 );

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
                       .orElseThrow(() -> new GlobalException(ErrorCode.USER_MAP_API_NOT_SUPPORTED));
    }

}
