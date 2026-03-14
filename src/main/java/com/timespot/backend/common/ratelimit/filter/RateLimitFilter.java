package com.timespot.backend.common.ratelimit.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timespot.backend.common.ratelimit.builder.RateLimitBucketBuilder;
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
import java.util.Map;
import lombok.RequiredArgsConstructor;
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

        BucketConfiguration config = findBucketConfiguration(requestURI);

        BucketProxy bucketProxy = (config != null)
                                  ? proxyManager.builder()
                                                .build(rateLimitKey, () -> config)
                                  : proxyManager.builder()
                                                .build(rateLimitKey, rateLimitBucketBuilder::getDefaultConfig);

        ConsumptionProbe probe = bucketProxy.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) filterChain.doFilter(request, response);
        else handleRateLimitExceeded(response, probe);
    }

    // ========================= 내부 메서드 =========================

    private boolean isExcludedPath(final String requestURI) {
        return requestURI.startsWith("/management")
               || requestURI.startsWith("/swagger-ui")
               || requestURI.startsWith("/v3/api-docs");
    }

    private BucketConfiguration findBucketConfiguration(final String requestURI) {
        Map<String, BucketConfiguration> configs = rateLimitBucketBuilder.getConfigurations();
        return configs.entrySet()
                      .stream()
                      .filter(entry -> requestURI.startsWith(entry.getKey()))
                      .max(Map.Entry.comparingByKey())
                      .map(Map.Entry::getValue)
                      .orElse(null);
    }

    private String resolveRateLimitKey(final HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
            && authentication.isAuthenticated()
            && authentication.getPrincipal() instanceof CustomUserDetails userDetails)
            return "user:" + userDetails.getId();

        return "ip:" + extractIpAddress(request);
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
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000L;
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(retryAfterSeconds));

        BaseResponse<Object> baseResponse = BaseResponse.error(ErrorCode.TOO_MANY_REQUESTS);

        objectMapper.writeValue(response.getWriter(), baseResponse);
    }

}
