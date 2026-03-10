package com.nomadspot.backend.common.config;

import com.nomadspot.backend.common.security.config.properties.CorsProperties;
import com.nomadspot.backend.common.security.jwt.provider.properties.JwtProperties;
import com.nomadspot.backend.infra.security.oauth.properties.OAuth2Properties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * PackageName : com.nomadspot.backend.common.config
 * FileName    : EnableConfigurationPropertiesConfig
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@Configuration
@EnableConfigurationProperties({CorsProperties.class,
                                JwtProperties.class,
                                OAuth2Properties.class})
public class EnableConfigurationPropertiesConfig {
}
