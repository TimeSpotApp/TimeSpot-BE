package com.timespot.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * PackageName : com.timespot.backend.common.config
 * FileName    : EnableJpaAuditingConfig
 * Author      : loadingKKamo21
 * Date        : 26. 2. 26.
 * Description : JPA Auditing 활성화 설정 (@CreatedDate, @LastModifiedDate 자동 관리)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 2. 26.    loadingKKamo21       Initial creation
 */
@Configuration
@EnableJpaAuditing
public class EnableJpaAuditingConfig {
}
