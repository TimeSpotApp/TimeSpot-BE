package com.timespot.backend.infra.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.api.async.RedisAsyncCommands;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
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

    @Bean
    public ProxyManager<String> proxyManager(final RedisConnectionFactory redisConnectionFactory) {
        return Bucket4jLettuce.casBasedBuilder(createAsyncCommands(redisConnectionFactory)).build();
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
