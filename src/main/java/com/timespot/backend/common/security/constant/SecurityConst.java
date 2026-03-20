package com.timespot.backend.common.security.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

/**
 * PackageName : com.timespot.backend.common.security.constant
 * FileName    : SecurityConst
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class SecurityConst {

    public static final String JWT_ACCESS_TOKEN_PREFIX = "Bearer ";
    public static final String JWT_ACCESS_TOKEN_HEADER = HttpHeaders.AUTHORIZATION;

    public static final String JWT_USERNAME_KEY    = "username";
    public static final String JWT_AUTHORITIES_KEY = "authorities";

    public static final String[] GET_PERMIT_ALL_URLS    = {
            // Common
            "/error",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            };
    public static final String[] POST_PERMIT_ALL_URLS   = {
            // Auth
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            };
    public static final String[] PUT_PERMIT_ALL_URLS    = {};
    public static final String[] PATCH_PERMIT_ALL_URLS  = {};
    public static final String[] DELETE_PERMIT_ALL_URLS = {};

    public static final String[] GET_AUTHENTICATED_URLS    = {
            // User
            "/api/v1/users"
    };
    public static final String[] POST_AUTHENTICATED_URLS   = {
            // Auth
            "/api/v1/auth/logout"
    };
    public static final String[] PUT_AUTHENTICATED_URLS    = {
            // User
            "/api/v1/users"
    };
    public static final String[] PATCH_AUTHENTICATED_URLS  = {};
    public static final String[] DELETE_AUTHENTICATED_URLS = {
            // User
            "/api/v1/users"
    };

    public static final String[] GET_ROLE_ADMIN_URLS    = {};
    public static final String[] POST_ROLE_ADMIN_URLS   = {};
    public static final String[] PUT_ROLE_ADMIN_URLS    = {};
    public static final String[] PATCH_ROLE_ADMIN_URLS  = {};
    public static final String[] DELETE_ROLE_ADMIN_URLS = {};

}
