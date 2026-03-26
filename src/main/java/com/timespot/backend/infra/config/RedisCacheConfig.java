package com.timespot.backend.infra.config;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * PackageName : com.timespot.backend.infra.config
 * FileName    : RedisCacheConfig
 * Author      : loadingKKamo21
 * Date        : 26. 3. 26.
 * Description : Redis 캐시 설정
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 26.    loadingKKamo21       Initial creation
 */
@Configuration
@RequiredArgsConstructor
public class RedisCacheConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public CacheManager cacheManager(final RedisConnectionFactory redisConnectionFactory) {
        ObjectMapper copiedObjectMapper = objectMapper.copy();
        copiedObjectMapper.activateDefaultTyping(BasicPolymorphicTypeValidator.builder()
                                                                              .allowIfBaseType(Object.class)
                                                                              .build(),
                                                 NON_FINAL,
                                                 PROPERTY);

        RedisCacheConfiguration defaultCacheConfig = createCacheConfiguration(Duration.ofHours(1), copiedObjectMapper);
        RedisCacheConfiguration dailyCacheConfig   = createCacheConfiguration(Duration.ofDays(1), copiedObjectMapper);

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("stations", dailyCacheConfig);

        return RedisCacheManager.builder(redisConnectionFactory)
                                .cacheDefaults(defaultCacheConfig)
                                .withInitialCacheConfigurations(cacheConfigurations)
                                .transactionAware()
                                .build();
    }

    @Bean
    public KeyGenerator customKeyGenerator() {
        return (target, method, params) -> target.getClass().getSimpleName() +
                                           "::" +
                                           method.getName() +
                                           "::" +
                                           Arrays.deepToString(params);
    }

    // ========================= 내부 메서드 =========================

    private RedisCacheConfiguration createCacheConfiguration(final Duration ttl,
                                                             final ObjectMapper objectMapper) {
        return RedisCacheConfiguration.defaultCacheConfig()
                                      .entryTtl(ttl)
                                      .serializeKeysWith(
                                              RedisSerializationContext.SerializationPair.fromSerializer(
                                                      new StringRedisSerializer()
                                              )
                                      )
                                      .serializeValuesWith(
                                              RedisSerializationContext.SerializationPair.fromSerializer(
                                                      new GenericJackson2JsonRedisSerializer(objectMapper)
                                              )
                                      );
    }

}
