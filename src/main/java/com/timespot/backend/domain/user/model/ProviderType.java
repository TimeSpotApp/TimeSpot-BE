package com.timespot.backend.domain.user.model;

import static com.timespot.backend.common.response.ErrorCode.SOCIAL_CONNECTION_PROVIDER_NOT_SUPPORTED;

import com.timespot.backend.common.error.GlobalException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * PackageName : com.timespot.backend.domain.user.model
 * FileName    : ProviderType
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description : 소셜 인증 제공자 유형 열거형 (APPLE, GOOGLE)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
public enum ProviderType {

    APPLE, GOOGLE;

    private static final Map<String, ProviderType> PROVIDER_TYPE_MAP = Stream.of(values())
                                                                             .collect(Collectors.toUnmodifiableMap(
                                                                                     Enum::name, Function.identity()
                                                                             ));

    /**
     * 소셜 인증 제공자 유형 조회
     *
     * @param provider 소셜 인증 제공자 유형 문자열
     * @return 소셜 인증 제공자 유형 enum
     */
    public static ProviderType from(final String provider) {
        return Optional.ofNullable(provider)
                       .map(String::toUpperCase)
                       .map(PROVIDER_TYPE_MAP::get)
                       .orElseThrow(() -> new GlobalException(SOCIAL_CONNECTION_PROVIDER_NOT_SUPPORTED));
    }


}
