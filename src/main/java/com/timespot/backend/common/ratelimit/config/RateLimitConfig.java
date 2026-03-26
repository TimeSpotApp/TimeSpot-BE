package com.timespot.backend.common.ratelimit.config;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timespot.backend.common.ratelimit.builder.RateLimitBucketBuilder;
import com.timespot.backend.common.ratelimit.filter.RateLimitFilter;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * PackageName : com.timespot.backend.common.ratelimit.config
 * FileName    : RateLimitConfig
 * Author      : loadingKKamo21
 * Date        : 26. 3. 15.
 * Description : Rate Limiting 필터 등록 설정
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 15.    loadingKKamo21       Initial creation
 */
@Configuration
@RequiredArgsConstructor
public class RateLimitConfig {

    private final ObjectMapper           objectMapper;
    private final ProxyManager<String>   proxyManager;
    private final RateLimitBucketBuilder rateLimitBucketBuilder;

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();

        registration.setFilter(new RateLimitFilter(objectMapper, proxyManager, rateLimitBucketBuilder));
        registration.addUrlPatterns("/*");
        registration.setOrder(HIGHEST_PRECEDENCE);

        return registration;
    }

}
