package com.timespot.backend.infra.config;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.api.async.RedisAsyncCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * PackageName : com.timespot.backend.infra.config
 * FileName    : Bucket4jConfig
 * Author      : loadingKKamo21
 * Date        : 26. 3. 14.
 * Description : Bucket4j Rate Limiting 설정 (Redis 기반 분산 Rate Limiter)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 14.    loadingKKamo21       Initial creation
 */
@Configuration
@Slf4j
public class Bucket4jConfig {

    @Bean
    public ProxyManager<String> proxyManager(final RedisConnectionFactory redisConnectionFactory) {
        return Bucket4jLettuce.casBasedBuilder(getRedisAsyncCommands(redisConnectionFactory))
                              .build()
                              .withMapper(key -> key.getBytes(UTF_8));
    }

    // ========================= 내부 메서드 =========================

    @SuppressWarnings("unchecked")
    private RedisAsyncCommands<byte[], byte[]> getRedisAsyncCommands(
            final RedisConnectionFactory redisConnectionFactory) {
        if (!(redisConnectionFactory instanceof LettuceConnectionFactory lettuceConnectionFactory))
            throw new IllegalStateException("RedisConnectionFactory must be LettuceConnectionFactory, but was: " +
                                            redisConnectionFactory.getClass().getName());
        try {
            var connection = lettuceConnectionFactory.getConnection();
            return (RedisAsyncCommands<byte[], byte[]>) connection.getNativeConnection();
        } catch (Exception e) {
            log.error("Failed to get Redis connection for Bucket4j: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to get Redis connection for Bucket4j", e);
        }
    }

}
