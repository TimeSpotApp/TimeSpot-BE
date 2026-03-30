package com.timespot.backend.common.security.constant;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.common.security.constant
 * FileName    : SecurityConst
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description : Spring Security 관련 상수 정의 (JWT, URL 패턴)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class SecurityConst {

    public static final String JWT_ACCESS_TOKEN_PREFIX = "Bearer ";
    public static final String JWT_ACCESS_TOKEN_HEADER = AUTHORIZATION;

    public static final String JWT_USERNAME_KEY    = "username";
    public static final String JWT_AUTHORITIES_KEY = "authorities";
    public static final String JWT_PROVIDER_KEY    = "provider";
    public static final String JWT_MAP_API_KEY     = "map_api";

    public static final String[] GET_PERMIT_ALL_URLS    = {
            // Common
            "/error",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            // Place
            "/api/v1/place",
            "/api/v1/place/detail",
            "/api/v1/place/search",
            "/api/v2/places",
            "/api/v2/places/**",
            // Station
            "/api/v1/stations",
    };
    public static final String[] POST_PERMIT_ALL_URLS   = {
            // Auth
            "/api/v1/auth/signup",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            // Device
            "/api/v1/auth/devices"
    };
    public static final String[] PUT_PERMIT_ALL_URLS    = {};
    public static final String[] PATCH_PERMIT_ALL_URLS  = {};
    public static final String[] DELETE_PERMIT_ALL_URLS = {
            // Device
            "/api/v1/auth/devices"
    };

    public static final String[] GET_AUTHENTICATED_URLS    = {
            // User
            "/api/v1/users",
            "/api/v1/users/notification-settings",
            // Place
            "/api/v1/place",
            "/api/v1/place/detail",
            // Favorite
            "/api/v1/favorites",
            // Visiting History
            "/api/v1/histories",
            "/api/v1/histories/{historyId}",
            // Station
            "/api/v1/stations"
    };
    public static final String[] POST_AUTHENTICATED_URLS   = {
            // Auth
            "/api/v1/auth/logout",
            // User
            "/api/v1/users",
            // Favorite
            "/api/v1/favorites",
            // Visiting History
            "/api/v1/histories"
    };
    public static final String[] PUT_AUTHENTICATED_URLS    = {
            // User
            "/api/v1/users",
            "/api/v1/users/notification-settings",
            // Visiting History
            "/api/v1/histories/{historyId}"
    };
    public static final String[] PATCH_AUTHENTICATED_URLS  = {};
    public static final String[] DELETE_AUTHENTICATED_URLS = {
            // User
            "/api/v1/users",
            // Favorite
            "/api/v1/favorites/{favoriteId}",
            // Visiting History
            "/api/v1/histories/{historyId}"
    };

    public static final String[] GET_ROLE_ADMIN_URLS    = {};
    public static final String[] POST_ROLE_ADMIN_URLS   = {};
    public static final String[] PUT_ROLE_ADMIN_URLS    = {};
    public static final String[] PATCH_ROLE_ADMIN_URLS  = {};
    public static final String[] DELETE_ROLE_ADMIN_URLS = {};

}
