package com.timespot.backend.infra.security.oauth.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * PackageName : com.timespot.backend.infra.security.oauth.properties
 * FileName    : OAuth2Properties
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description : OAuth2 인증 설정 속성 (Apple, Google)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@ConfigurationProperties(prefix = "app.oauth2")
@Getter
@Setter
public final class OAuth2Properties {

    private Apple  apple;
    private Google google;

    @Getter
    @Setter
    public static class Apple {
        private String clientId;
        private String clientSecret;
        private String teamId;
        private String keyId;
        private String privateKey;
        private String redirectUri;
    }

    @Getter
    @Setter
    public static class Google {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
    }

}
