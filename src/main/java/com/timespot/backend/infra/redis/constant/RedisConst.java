package com.timespot.backend.infra.redis.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.infra.redis.constant
 * FileName    : RedisConst
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description : Redis 키 접두사 상수 정의 (JWT 토큰 관리)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class RedisConst {

    public static final String JWT_REFRESH_TOKEN_PREFIX          = "jwt:refresh:";
    public static final String JWT_ACCESS_TOKEN_BLACKLIST_PREFIX = "jwt:access:blacklist:";

}
