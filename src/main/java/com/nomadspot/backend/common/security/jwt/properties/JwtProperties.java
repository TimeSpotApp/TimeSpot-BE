package com.nomadspot.backend.common.security.jwt.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * PackageName : com.nomadspot.backend.common.security.jwt.properties
 * FileName    : JwtProperties
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {

    private String issuer;
    private String accessTokenSecret;
    private String refreshTokenSecret;
    private long   accessTokenExpirationSeconds;
    private long   refreshTokenExpirationSeconds;

}
