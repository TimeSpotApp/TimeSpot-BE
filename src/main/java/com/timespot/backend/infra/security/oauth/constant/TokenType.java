package com.timespot.backend.infra.security.oauth.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.timespot.backend.infra.security.oauth.constant
 * FileName    : TokenType
 * Author      : loadingKKamo21
 * Date        : 26. 3. 12.
 * Description : 토큰 유형 열거형 (access_token, refresh_token)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 12.    loadingKKamo21       Initial creation
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TokenType {

    ACCESS_TOKEN("access_token"), REFRESH_TOKEN("refresh_token");

    private final String value;

}
