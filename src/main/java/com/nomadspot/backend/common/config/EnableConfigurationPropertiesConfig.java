package com.nomadspot.backend.common.config;

import com.nomadspot.backend.common.security.config.properties.CorsProperties;
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
@EnableConfigurationProperties({CorsProperties.class})
public class EnableConfigurationPropertiesConfig {
}
