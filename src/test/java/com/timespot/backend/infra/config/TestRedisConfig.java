package com.timespot.backend.infra.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import java.io.IOException;
import java.net.ServerSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.embedded.RedisServer;

/**
 * PackageName : com.timespot.backend.infra.config
 * FileName    : TestRedisConfig
 * Author      : loadingKKamo21
 * Date        : 26. 3. 15.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 15.    loadingKKamo21       Initial creation
 */
@Profile("test")
@TestConfiguration
@RequiredArgsConstructor
@Slf4j
public class TestRedisConfig {

    private final ObjectMapper objectMapper;

    @Bean(destroyMethod = "stop")
    public RedisServer redisServer() throws IOException {
        int redisPort = findAvailablePort();
        log.info("Found available port for Embedded Redis: {}", redisPort);

        RedisServer redisServer = new RedisServer(redisPort);

        try {
            redisServer.start();
            return redisServer;
        } catch (Exception e) {
            throw new RuntimeException("Embedded Redis server failed to start", e);
        }
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(final RedisServer redisServer) {
        final int port = redisServer.ports().get(0);
        log.info("Connecting to Embedded Redis on port: {}", port);
        return new LettuceConnectionFactory("localhost", port);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(final RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        ObjectMapper copiedObjectMapper = objectMapper.copy();
        copiedObjectMapper.activateDefaultTyping(BasicPolymorphicTypeValidator.builder()
                                                                              .allowIfBaseType(Object.class)
                                                                              .build(),
                                                 DefaultTyping.NON_FINAL);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(copiedObjectMapper));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(copiedObjectMapper));

        return template;
    }

    private int findAvailablePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

}
