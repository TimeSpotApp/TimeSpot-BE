package com.timespot.backend.infra.google.places.client.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * PackageName : com.timespot.backend.infra.google.places.client.properties
 * FileName    : GooglePlacesProperties
 * Author      : loadingKKamo21
 * Date        : 26. 3. 30.
 * Description : Google Places API 설정 속성
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 30.    loadingKKamo21       Initial creation
 */
@ConfigurationProperties(prefix = "google.places")
@Getter
@Setter
public class GooglePlacesProperties {

    private String apiKey;
    private String baseUrl;
    private int    searchRadiusMeters = 50;
    private int    cacheTtlDays       = 30;

}
