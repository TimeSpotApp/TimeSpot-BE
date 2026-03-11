package com.timespot.backend.common.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * PackageName : com.timespot.backend.common.config
 * FileName    : QuerydslConfig
 * Author      : loadingKKamo21
 * Date        : 26. 2. 26.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 2. 26.    loadingKKamo21       Initial creation
 */
@Configuration
public class QuerydslConfig {

    @Bean
    public JPAQueryFactory jpaQueryFactory(final EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }

}
