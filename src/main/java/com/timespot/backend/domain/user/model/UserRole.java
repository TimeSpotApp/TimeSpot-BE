package com.timespot.backend.domain.user.model;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.response.ErrorCode;
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
 * FileName    : UserRole
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserRole {

    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private static final Map<String, UserRole> USER_ROLE_MAP = Stream.of(values())
                                                                     .collect(Collectors.toUnmodifiableMap(
                                                                             Enum::name, Function.identity()
                                                                     ));

    private final String authority;

    /**
     * 계정 유형 조회
     *
     * @param authority 계정 유형 문자열
     * @return 계정 유형 enum
     */
    public static UserRole from(final String authority) {
        return Optional.ofNullable(authority)
                       .map(String::toUpperCase)
                       .map(USER_ROLE_MAP::get)
                       .orElseThrow(() -> new GlobalException(ErrorCode.USER_ROLE_NOT_SUPPORTED));
    }

}
