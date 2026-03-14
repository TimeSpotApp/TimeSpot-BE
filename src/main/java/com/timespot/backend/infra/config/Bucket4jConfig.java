package com.timespot.backend.infra.config;

import com.timespot.backend.common.ratelimit.builder.RateLimitBucketBuilder;
import com.timespot.backend.common.ratelimit.properties.RateLimitProperties;
import com.timespot.backend.common.ratelimit.properties.RateLimitProperties.CustomConfig;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.api.async.RedisAsyncCommands;
import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * PackageName : com.timespot.backend.infra.config
 * FileName    : Bucket4jConfig
 * Author      : loadingKKamo21
 * Date        : 26. 3. 14.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 14.    loadingKKamo21       Initial creation
 */
@Configuration
@RequiredArgsConstructor
public class Bucket4jConfig {

    private final RateLimitProperties rateLimitProperties;

    @Value("${spring.data.redis.host}")
    private String redisHost;
    @Value("${spring.data.redis.port}")
    private int    redisPort;
    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean
    public ProxyManager<String> bucket4jProxyManager(final RedisConnectionFactory redisConnectionFactory) {
        return Bucket4jLettuce.casBasedBuilder(createAsyncCommands(redisConnectionFactory)).build();
    }

    @Bean
    public RateLimitBucketBuilder rateLimitBucketBuilder() {
        RateLimitBucketBuilder builder = new RateLimitBucketBuilder();

        for (CustomConfig config : rateLimitProperties.getCustomConfigs())
            if (config.isFixedRefill())
                builder.addConfigWithFixedRefill(
                        config.getPathPattern(),
                        config.getCapacity(),
                        Duration.ofMinutes(config.getDurationMinutes())
                );
            else
                builder.addConfig(
                        config.getPathPattern(),
                        config.getCapacity(),
                        Duration.ofMinutes(config.getDurationMinutes())
                );

        return builder;
    }

    // ========================= 내부 메서드 =========================

    @SuppressWarnings("unchecked")
    private RedisAsyncCommands<String, byte[]> createAsyncCommands(
            final RedisConnectionFactory redisConnectionFactory
    ) {
        if (redisConnectionFactory instanceof LettuceConnectionFactory lettuceConnectionFactory) {
            var connection = Objects.requireNonNull(lettuceConnectionFactory.getConnection());

            return (RedisAsyncCommands<String, byte[]>) connection.getNativeConnection();
        }

        throw new IllegalStateException(
                "RedisConnectionFactory must be LettuceConnectionFactory, but was: " +
                redisConnectionFactory.getClass().getName()
        );
    }

}
