package com.timespot.backend.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * PackageName : com.timespot.backend.infra.config
 * FileName    : RedisGeoConfig
 * Author      : loadingKKamo21
 * Date        : 26. 3. 29.
 * Description : Redis GEO 연동 설정
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 29.    loadingKKamo21       Initial creation
 */
@Configuration
public class RedisGeoConfig {

    /**
     * GEO 전용 RedisTemplate
     * - String 직렬화 사용 (메모리 효율, 성능 최적화)
     * - Redis GEO 명령어 opsForGeo() 활용
     */
    @Bean
    public RedisTemplate<String, String> redisTemplateForGeo(final RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        return template;
    }

}
