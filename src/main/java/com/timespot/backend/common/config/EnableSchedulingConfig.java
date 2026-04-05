package com.timespot.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * PackageName : com.timespot.backend.common.config
 * FileName    : EnableSchedulingConfig
 * Author      : loadingKKamo21
 * Date        : 26. 4. 5.
 * Description : 스케줄링 활성화 설정
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 4. 5.     loadingKKamo21       Initial creation
 */
@Profile("!test")
@Configuration
@EnableScheduling
public class EnableSchedulingConfig {
}
