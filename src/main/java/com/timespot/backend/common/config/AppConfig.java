package com.timespot.backend.common.config;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * PackageName : com.timespot.backend.common.config
 * FileName    : AppConfig
 * Author      : loadingKKamo21
 * Date        : 26. 2. 26.
 * Description : 애플리케이션 공통 설정 (Jackson ObjectMapper 커스터마이징)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 2. 26.    loadingKKamo21       Initial creation
 */
@Configuration
public class AppConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            builder.modules(new JavaTimeModule());
            builder.featuresToDisable(WRITE_DATES_AS_TIMESTAMPS);
            builder.serializationInclusion(NON_NULL);
        };
    }

}
