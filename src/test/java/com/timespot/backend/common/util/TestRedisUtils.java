package com.timespot.backend.common.util;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * PackageName : com.timespot.backend.common.util
 * FileName    : TestRedisUtils
 * Author      : loadingKKamo21
 * Date        : 26. 3. 15.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 15.    loadingKKamo21       Initial creation
 */
@TestComponent
public class TestRedisUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void flushAll() {
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        Objects.requireNonNull(connectionFactory, "RedisConnectionFactory must not be null");

        try (RedisConnection connection = connectionFactory.getConnection()) {
            connection.serverCommands().flushAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to flush Redis", e);
        }
    }

}
