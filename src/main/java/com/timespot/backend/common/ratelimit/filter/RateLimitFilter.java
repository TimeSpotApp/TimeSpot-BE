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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
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

    private static final long LOG_THROTTLE_INTERVAL_MILLIS = TimeUnit.MINUTES.toMillis(1);

    private final ObjectMapper objectMapper;

    private final ProxyManager<String>   proxyManager;
    private final RateLimitBucketBuilder rateLimitBucketBuilder;

    private final Map<String, Long> lastLogTimestamps = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        final String requestURI = request.getRequestURI();

        if (isExcludedPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String rateLimitKey = null;
        try {
            rateLimitKey = resolveRateLimitKey(request);
            BucketConfiguration config = resolveBucketConfiguration(requestURI);

            BucketProxy bucketProxy = proxyManager.builder().build(rateLimitKey, () -> config);

            ConsumptionProbe probe = bucketProxy.tryConsumeAndReturnRemaining(1);

            if (probe.isConsumed()) {
                response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
                filterChain.doFilter(request, response);
            } else handleRateLimitExceeded(response, probe, rateLimitKey);
        } catch (Exception e) {
            log.warn("Rate limit processing failed. Allowing request to proceed: {} - {}",
                     rateLimitKey != null ? rateLimitKey : "unknown",
                     e.getMessage());
            filterChain.doFilter(request, response);
        }
    }

    // ========================= 내부 메서드 =========================

    /**
     * 제외 경로 확인
     *
     * @param requestURI 요청 URI
     * @return 제외 경로 여부
     */
    private boolean isExcludedPath(final String requestURI) {
        return Arrays.stream(RateLimitConst.excludedPathPrefixes)
                     .anyMatch(requestURI::startsWith);
    }

    /**
     * 요청 URI와 인증 상태에 따른 BucketConfiguration 설정
     *
     * @param requestURI 요청 URI
     * @return BucketConfiguration
     */
    private BucketConfiguration resolveBucketConfiguration(final String requestURI) {
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

    /**
     * Rate Limit 키 생성
     *
     * @param request HTTP 요청
     * @return Rate Limit 키
     */
    private String resolveRateLimitKey(final HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
            && authentication.isAuthenticated()
            && authentication.getPrincipal() instanceof CustomUserDetails userDetails)
            return RateLimitConst.AUTHENTICATED_KEY_PREFIX + userDetails.getId();

        return RateLimitConst.ANONYMOUS_KEY_PREFIX + extractIpAddress(request);
    }

    /**
     * 클라이언트 IP 주소 추출
     * 프록시 환경 (X-Forwarded-For, X-Real-IP) 고려
     *
     * @param request HTTP 요청
     * @return 클라이언트 IP 주소
     */
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

    /**
     * Rate Limit 초과 처리
     * 429 Too Many Requests 응답 반환하며, 로그는 1분에 1회만 출력하여 로그 과다 방지
     *
     * @param response     HTTP 응답
     * @param probe        Consumption Probe
     * @param rateLimitKey Rate Limit 키
     * @throws IOException IOException
     */
    private void handleRateLimitExceeded(final HttpServletResponse response, final ConsumptionProbe probe,
                                         final String rateLimitKey)
    throws IOException {
        final long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000L;

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(retryAfterSeconds));

        BaseResponse<Object> baseResponse = BaseResponse.error(ErrorCode.TOO_MANY_REQUESTS);

        objectMapper.writeValue(response.getWriter(), baseResponse);

        logRateLimitExceeded(rateLimitKey, retryAfterSeconds);
    }

    /**
     * Rate Limit 초과 로그 출력
     * 동일한 RateLimitKey에 대해 1분에 1회만 WARN 로그 출력
     * 나머지 시간은 DEBUG 로그로 기록
     *
     * @param rateLimitKey      Rate Limit 키
     * @param retryAfterSeconds 재시도까지의 시간(초)
     */
    private void logRateLimitExceeded(final String rateLimitKey, final long retryAfterSeconds) {
        final long now     = System.currentTimeMillis();
        final Long lastLog = lastLogTimestamps.get(rateLimitKey);

        if (lastLog == null || (now - lastLog) >= LOG_THROTTLE_INTERVAL_MILLIS) {
            Long previous = lastLogTimestamps.putIfAbsent(rateLimitKey, now);
            if (previous == null)
                log.warn("Rate limit exceeded for key: {}. Retry after: {} seconds",
                         rateLimitKey,
                         retryAfterSeconds);
            else
                log.debug("Rate limit exceeded for key: {}. Retry after: {} seconds (throttled)",
                          rateLimitKey,
                          retryAfterSeconds);
        } else
            log.debug("Rate limit exceeded for key: {}. Retry after: {} seconds (throttled)",
                      rateLimitKey,
                      retryAfterSeconds);
    }

}
