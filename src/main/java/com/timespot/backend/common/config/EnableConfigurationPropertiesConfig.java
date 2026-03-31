package com.timespot.backend.common.config;

import com.timespot.backend.common.ratelimit.properties.RateLimitProperties;
import com.timespot.backend.common.security.config.properties.CorsProperties;
import com.timespot.backend.common.security.jwt.provider.properties.JwtProperties;
import com.timespot.backend.infra.google.places.client.properties.GooglePlacesProperties;
import com.timespot.backend.infra.security.oauth.properties.OAuth2Properties;
import com.timespot.backend.infra.visitkorea.client.properties.VisitKoreaProperties;
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
 * 26. 3. 30.    loadingKKamo21       GooglePlacesProperties 추가
 */
@Configuration
@EnableConfigurationProperties({CorsProperties.class,
                                JwtProperties.class,
                                OAuth2Properties.class,
                                RateLimitProperties.class,
                                VisitKoreaProperties.class,
                                GooglePlacesProperties.class})
public class EnableConfigurationPropertiesConfig {
}
