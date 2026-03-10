package com.nomadspot.backend.domain.user.model;

import com.nomadspot.backend.common.error.GlobalException;
import com.nomadspot.backend.common.response.ErrorCode;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * PackageName : com.nomadspot.backend.domain.user.model
 * FileName    : ProviderType
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description :
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
        return Optional.ofNullable(PROVIDER_TYPE_MAP.get(provider.toUpperCase()))
                       .orElseThrow(() -> new GlobalException(ErrorCode.SOCIAL_CONNECTION_PROVIDER_NOT_SUPPORTED));
    }


}
