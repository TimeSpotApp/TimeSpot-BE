package com.timespot.backend.common.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timespot.backend.common.security.config.properties.CorsProperties;
import com.timespot.backend.common.security.constant.SecurityConst;
import com.timespot.backend.common.security.entrypoint.CustomAuthenticationEntryPoint;
import com.timespot.backend.common.security.handler.CustomAccessDeniedHandler;
import com.timespot.backend.common.security.jwt.filter.JwtAuthenticationFilter;
import com.timespot.backend.common.security.jwt.filter.JwtExceptionFilter;
import com.timespot.backend.common.security.jwt.provider.JwtProvider;
import com.timespot.backend.domain.user.model.UserRole;
import com.timespot.backend.infra.redis.dao.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * PackageName : com.timespot.backend.common.security.config
 * FileName    : SecurityConfig
 * Author      : loadingKKamo21
 * Date        : 26. 2. 28.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 2. 28.    loadingKKamo21       Initial creation
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper    objectMapper;
    private final JwtProvider     jwtProvider;
    private final RedisRepository redisRepository;

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler      accessDeniedHandler;

    @Value("${management.endpoints.web.base-path}")
    private String actuatorBasePath;

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(final HttpSecurity http) throws Exception {
        final String actuatorPathPattern = actuatorBasePath + "/**";
        final String healthPath          = actuatorBasePath + "/health";

        http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .securityMatcher(actuatorPathPattern)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(healthPath).permitAll()
                        .requestMatchers(actuatorPathPattern).hasAuthority(UserRole.ADMIN.getAuthority())
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(final HttpSecurity http,
                                                   final CorsConfigurationSource corsConfigurationSource)
    throws Exception {
        final JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtProvider, redisRepository);
        final JwtExceptionFilter jwtExceptionFilter = new JwtExceptionFilter(objectMapper);

        http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                .logout(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // Permit All
                        .requestMatchers(HttpMethod.GET, SecurityConst.GET_PERMIT_ALL_URLS).permitAll()
                        .requestMatchers(HttpMethod.POST, SecurityConst.POST_PERMIT_ALL_URLS).permitAll()
                        //.requestMatchers(HttpMethod.PUT, SecurityConst.PUT_PERMIT_ALL_URLS).permitAll()
                        //.requestMatchers(HttpMethod.PATCH, SecurityConst.PATCH_PERMIT_ALL_URLS).permitAll()
                        //.requestMatchers(HttpMethod.DELETE, SecurityConst.DELETE_PERMIT_ALL_URLS).permitAll()

                        // Authenticated
                        .requestMatchers(HttpMethod.GET, SecurityConst.GET_AUTHENTICATED_URLS).authenticated()
                        .requestMatchers(HttpMethod.POST, SecurityConst.POST_AUTHENTICATED_URLS).authenticated()
                        .requestMatchers(HttpMethod.PUT, SecurityConst.PUT_AUTHENTICATED_URLS).authenticated()
                        //.requestMatchers(HttpMethod.PATCH, SecurityConst.PATCH_AUTHENTICATED_URLS).authenticated()
                        .requestMatchers(HttpMethod.DELETE, SecurityConst.DELETE_AUTHENTICATED_URLS).authenticated()

                        // Role Admin Only
                        //.requestMatchers(HttpMethod.GET, SecurityConst.GET_ROLE_ADMIN_URLS).hasAuthority(UserRole.ADMIN.getAuthority())
                        //.requestMatchers(HttpMethod.POST, SecurityConst.POST_ROLE_ADMIN_URLS).hasAuthority(UserRole.ADMIN.getAuthority())
                        //.requestMatchers(HttpMethod.PUT, SecurityConst.PUT_ROLE_ADMIN_URLS).hasAuthority(UserRole.ADMIN.getAuthority())
                        //.requestMatchers(HttpMethod.PATCH, SecurityConst.PATCH_ROLE_ADMIN_URLS).hasAuthority(UserRole.ADMIN.getAuthority())
                        //.requestMatchers(HttpMethod.DELETE, SecurityConst.DELETE_ROLE_ADMIN_URLS).hasAuthority(UserRole.ADMIN.getAuthority())

                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint)
                                                         .accessDeniedHandler(accessDeniedHandler))

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, jwtAuthenticationFilter.getClass());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(final CorsProperties corsProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setExposedHeaders(corsProperties.getExposedHeaders());
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
