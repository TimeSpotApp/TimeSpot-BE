package com.timespot.backend.common.ratelimit.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.common.ratelimit.constant
 * FileName    : RateLimitConst
 * Author      : loadingKKamo21
 * Date        : 26. 3. 15.
 * Description : Rate Limiting 상수 정의 (리필 전략, 키 접두사, 제외 경로)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 15.    loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class RateLimitConst {

    public static final String REFILL_STRATEGY_GREEDY = "greedy";
    public static final String REFILL_STRATEGY_FIXED  = "fixed";

    public static final String ANONYMOUS_KEY_PREFIX     = "ip:";
    public static final String AUTHENTICATED_KEY_PREFIX = "user:";

    public static final String[] excludedPathPrefixes = {
            "/management",
            "/swagger-ui",
            "/v3/api-docs"
    };

}
