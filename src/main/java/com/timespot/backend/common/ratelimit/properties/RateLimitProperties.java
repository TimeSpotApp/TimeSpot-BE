package com.timespot.backend.common.ratelimit.properties;

import static com.timespot.backend.common.ratelimit.constant.RateLimitConst.REFILL_STRATEGY_GREEDY;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * PackageName : com.timespot.backend.common.ratelimit.properties
 * FileName    : RateLimitProperties
 * Author      : loadingKKamo21
 * Date        : 26. 3. 14.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 14.    loadingKKamo21       Initial creation
 */
@ConfigurationProperties(prefix = "app.rate-limit")
@Getter
@Setter
public final class RateLimitProperties {

    private BucketConfig         defaultConfig = new BucketConfig();
    private BucketConfig         anonymous     = new BucketConfig();
    private BucketConfig         authenticated = new BucketConfig();
    private List<EndpointConfig> endpoints     = new ArrayList<>();

    public EndpointConfig findEndpointConfig(final String requestURI) {
        return endpoints.stream()
                        .filter(config -> requestURI.startsWith(config.getPathPattern()))
                        .max(Comparator.comparingInt(o -> o.getPathPattern().length()))
                        .orElse(null);
    }

    @Getter
    @Setter
    public static class BucketConfig {

        private long   capacity        = 100L;
        private long   durationMinutes = 1L;
        private String refillStrategy  = REFILL_STRATEGY_GREEDY;
        private String description     = "";

    }

    @Getter
    @Setter
    public static class EndpointConfig extends BucketConfig {
        private String pathPattern;
    }

}
