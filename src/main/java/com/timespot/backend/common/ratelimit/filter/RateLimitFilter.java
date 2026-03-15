package com.timespot.backend.common.ratelimit.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timespot.backend.common.ratelimit.builder.RateLimitBucketBuilder;
import com.timespot.backend.common.ratelimit.constant.RateLimitConst;
import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.ErrorCode;
import com.timespot.backend.common.security.model.CustomUserDetails;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * PackageName : com.timespot.backend.common.ratelimit.filter
 * FileName    : RateLimitFilter
 * Author      : loadingKKamo21
 * Date        : 26. 3. 15.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 15.    loadingKKamo21       Initial creation
 */
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    private final ProxyManager<String>   proxyManager;
    private final RateLimitBucketBuilder rateLimitBucketBuilder;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        final String requestURI = request.getRequestURI();

        if (isExcludedPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String rateLimitKey = resolveRateLimitKey(request);

        BucketConfiguration config = resolveBucketConfiguration(requestURI, request);

        BucketProxy bucketProxy = proxyManager.builder().build(rateLimitKey, () -> config);

        ConsumptionProbe probe = bucketProxy.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else handleRateLimitExceeded(response, probe);
    }

    // ========================= 내부 메서드 =========================

    private boolean isExcludedPath(final String requestURI) {
        return Arrays.stream(RateLimitConst.excludedPathPrefixes)
                     .anyMatch(requestURI::startsWith);
    }

    private BucketConfiguration resolveBucketConfiguration(final String requestURI, final HttpServletRequest request) {
        final Map<String, BucketConfiguration> endpointConfigs = rateLimitBucketBuilder.getConfigurations();

        for (Entry<String, BucketConfiguration> entry : endpointConfigs.entrySet())
            if (requestURI.startsWith(entry.getKey())) {
                log.debug("Endpoint-specific rate limit applied: {} -> {}", requestURI, entry.getKey());
                return entry.getValue();
            }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null
                                  && authentication.isAuthenticated()
                                  && authentication.getPrincipal() instanceof CustomUserDetails;

        if (isAuthenticated) {
            log.debug("Authenticated user rate limit applied for: {}", requestURI);
            return rateLimitBucketBuilder.getAuthenticatedConfig();
        } else {
            log.debug("Anonymous user rate limit applied for: {}", requestURI);
            return rateLimitBucketBuilder.getAnonymousConfig();
        }
    }

    private String resolveRateLimitKey(final HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
            && authentication.isAuthenticated()
            && authentication.getPrincipal() instanceof CustomUserDetails userDetails)
            return RateLimitConst.AUTHENTICATED_KEY_PREFIX + userDetails.getId();

        return RateLimitConst.ANONYMOUS_KEY_PREFIX + extractIpAddress(request);
    }

    private String extractIpAddress(final HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("X-Real-IP");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip))
            ip = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(ip))
            ip = "127.0.0.1";

        return ip;
    }

    private void handleRateLimitExceeded(final HttpServletResponse response, final ConsumptionProbe probe)
    throws IOException {
        final long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000L;

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(retryAfterSeconds));

        BaseResponse<Object> baseResponse = BaseResponse.error(ErrorCode.TOO_MANY_REQUESTS);

        objectMapper.writeValue(response.getWriter(), baseResponse);

        log.warn("Rate limit exceeded. Retry after: {} seconds", retryAfterSeconds);
    }

}
