package com.timespot.backend.common.ratelimit.properties;

import java.util.ArrayList;
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

    private List<CustomConfig> customConfigs = new ArrayList<>();

    @Getter
    @Setter
    public static class CustomConfig {

        private String  pathPattern;
        private long    capacity;
        private long    durationMinutes;
        private boolean fixedRefill = false;

    }

}
