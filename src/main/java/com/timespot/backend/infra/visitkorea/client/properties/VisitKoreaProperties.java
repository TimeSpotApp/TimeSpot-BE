package com.timespot.backend.infra.visitkorea.client.properties;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLEncoder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * PackageName : com.timespot.backend.infra.visitkorea.client.properties
 * FileName    : VisitKoreaProperties
 * Author      : loadingKKamo21
 * Date        : 26. 3. 29.
 * Description : VisitKorea API 설정 속성
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 29.    loadingKKamo21       Initial creation
 */
@ConfigurationProperties(prefix = "app.api.visit-korea")
@Getter
@Setter
public class VisitKoreaProperties {

    private String  serviceKey;
    private String  baseUrl;
    private Integer maxRadiusMeters;
    private Integer defaultRadiusMeters;

    public String getServiceKeyEncoded() {
        return URLEncoder.encode(serviceKey, UTF_8);
    }

}
