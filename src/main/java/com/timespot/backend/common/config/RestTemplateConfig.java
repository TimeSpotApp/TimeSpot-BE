package com.timespot.backend.common.config; // 프로젝트 구조에 맞게 패키지명 수정

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * PackageName : com.timespot.backend.common.config
 * FileName    : RestTemplateConfig
 * Author      : whitecity01
 * Date        : 26. 3. 22.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 22.     whitecity01       ADD place detail
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}