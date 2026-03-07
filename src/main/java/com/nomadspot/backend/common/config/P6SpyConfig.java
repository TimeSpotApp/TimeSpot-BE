package com.nomadspot.backend.common.config;

import com.nomadspot.backend.common.config.formatter.P6SpySqlFormatter;
import com.p6spy.engine.spy.P6SpyOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * PackageName : com.nomadspot.backend.common.config
 * FileName    : P6SpyConfig
 * Author      : loadingKKamo21
 * Date        : 26. 3. 7.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 7.     loadingKKamo21       Initial creation
 */
@Profile("!prod")
@Configuration
public class P6SpyConfig {

    @PostConstruct
    private void setLogMessageFormat() {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(P6SpySqlFormatter.class.getName());
    }

}
