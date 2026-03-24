package com.timespot.backend.common.config;

import com.timespot.backend.common.ratelimit.properties.RateLimitProperties;
import com.timespot.backend.common.security.config.properties.CorsProperties;
import com.timespot.backend.common.security.jwt.provider.properties.JwtProperties;
import com.timespot.backend.infra.security.oauth.properties.OAuth2Properties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * PackageName : com.timespot.backend.common.config
 * FileName    : EnableConfigurationPropertiesConfig
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description : @ConfigurationProperties 클래스 활성화 설정
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@Configuration
@EnableConfigurationProperties({CorsProperties.class,
                                JwtProperties.class,
                                OAuth2Properties.class,
                                RateLimitProperties.class})
public class EnableConfigurationPropertiesConfig {
}
