package com.timespot.backend.infra.apns.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.apns")
public class ApnsProperties {

    @NotBlank
    private String teamId;

    @NotBlank
    private String keyId;

    @NotBlank
    private String privateKey;

    @NotBlank
    private String bundleId;

    @NotBlank
    private String endpoint;
}
