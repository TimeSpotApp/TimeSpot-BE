package com.nomadspot.backend.common.security.jwt.provider.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * PackageName : com.nomadspot.backend.common.security.jwt.provider.properties
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
public final class JwtProperties {

    @NotBlank
    private String issuer;
    @NotBlank
    private String accessTokenSecret;
    @NotBlank
    private String refreshTokenSecret;
    @NotNull
    @Min(1)
    private Long   accessTokenExpirationSeconds;
    @NotNull
    @Min(1)
    private Long   refreshTokenExpirationSeconds;

}
