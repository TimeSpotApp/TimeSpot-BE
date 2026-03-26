package com.timespot.backend.common.security.config.properties;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * PackageName : com.timespot.backend.common.security.config.properties
 * FileName    : CorsProperties
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description : CORS 설정 속성 (허용 오리진, 메서드, 헤더)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@ConfigurationProperties(prefix = "app.cors")
@Getter
@Setter
public final class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>();
    private List<String> allowedMethods = new ArrayList<>();
    private List<String> allowedHeaders = new ArrayList<>();
    private List<String> exposedHeaders = new ArrayList<>();

}
