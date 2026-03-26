package com.timespot.backend.common.ratelimit.filter;

import static com.timespot.backend.common.ratelimit.constant.RateLimitConst.ANONYMOUS_KEY_PREFIX;
import static com.timespot.backend.common.ratelimit.constant.RateLimitConst.excludedPathPrefixes;
import static com.timespot.backend.common.response.ErrorCode.TOO_MANY_REQUESTS;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timespot.backend.common.ratelimit.builder.RateLimitBucketBuilder;
import com.timespot.backend.common.response.BaseResponse;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * PackageName : com.timespot.backend.common.ratelimit.filter
 * FileName    : RateLimitFilter
 * Author      : loadingKKamo21
 * Date        : 26. 3. 15.
 * Description : Rate Limiting 필터 (Bucket4j + Redis)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 15.    loadingKKamo21       Initial creation
 * 26. 3. 24.    loadingKKamo21       코드 최적화 (메서드 분리, 주석 개선)
 */
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final PathPatternParser pathPatternParser = new PathPatternParser();
    private final AntPathMatcher    antPathMatcher    = new AntPathMatcher();

    private final ObjectMapper           objectMapper;
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

        String              rateLimitKey = resolveRateLimitKey(request);
        BucketConfiguration config       = resolveBucketConfiguration(requestURI);

        try {
            BucketProxy      bucketProxy = proxyManager.builder().build(rateLimitKey, () -> config);
            ConsumptionProbe probe       = bucketProxy.tryConsumeAndReturnRemaining(1);

            if (probe.isConsumed()) {
                response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
                filterChain.doFilter(request, response);
            } else handleRateLimitExceeded(response, probe, rateLimitKey);
        } catch (Exception e) {
            log.warn("Rate limit check failed for {}. Proceeding request.", rateLimitKey, e);
            filterChain.doFilter(request, response);
        }
    }

    // ========================= 내부 메서드 =========================

    /**
     * 제외 경로 여부 확인
     */
    private boolean isExcludedPath(final String requestURI) {
        return Arrays.stream(excludedPathPrefixes).anyMatch(requestURI::startsWith);
    }

    /**
     * 요청 URI 에 적합한 BucketConfiguration 해결
     * <p>
     * 우선순위:
     * 1. PathPattern 매칭 (정확도 우선)
     * 2. AntPath 매칭 (정확도 우선)
     * 3. Anonymous 기본 설정
     * </p>
     */
    private BucketConfiguration resolveBucketConfiguration(final String requestURI) {
        return findEndpointConfigByPattern(requestURI).or(() -> findEndpointConfigByAntPath(requestURI))
                                                      .orElse(rateLimitBucketBuilder.getAnonymousConfig());
    }

    /**
     * PathPattern 으로 엔드포인트 설정 검색
     */
    private Optional<BucketConfiguration> findEndpointConfigByPattern(final String requestURI) {
        return rateLimitBucketBuilder.getEndpointConfigs()
                                     .entrySet()
                                     .stream()
                                     .filter(entry -> {
                                         try {
                                             return pathPatternParser.parse(entry.getKey())
                                                                     .matches(PathContainer.parsePath(requestURI));
                                         } catch (Exception e) {
                                             log.warn("PathPattern parsing failed for pattern: {}, URI: {}",
                                                      entry.getKey(), requestURI, e);
                                             return false;
                                         }
                                     })
                                     .max(Comparator.comparingInt(e -> e.getKey().length()))
                                     .map(Map.Entry::getValue);
    }

    /**
     * AntPath 로 엔드포인트 설정 검색
     */
    private Optional<BucketConfiguration> findEndpointConfigByAntPath(final String requestURI) {
        return rateLimitBucketBuilder.getEndpointConfigs()
                                     .entrySet()
                                     .stream()
                                     .filter(entry -> antPathMatcher.match(entry.getKey(), requestURI))
                                     .max(Comparator.comparingInt(e -> e.getKey().length()))
                                     .map(Map.Entry::getValue);
    }

    /**
     * Rate Limit 키 생성 (익명 사용자: IP 기반)
     */
    private String resolveRateLimitKey(final HttpServletRequest request) {
        return ANONYMOUS_KEY_PREFIX + extractIpAddress(request);
    }

    /**
     * 클라이언트 IP 주소 추출
     * <p>
     * 프록시 환경 (X-Forwarded-For, X-Real-IP) 고려
     * </p>
     */
    private String extractIpAddress(final HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip))
            return ip.split(",")[0].trim();

        ip = request.getHeader("X-Real-IP");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip))
            ip = request.getRemoteAddr();

        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    /**
     * Rate Limit 초과 처리
     * <p>
     * - 429 Too Many Requests 응답 반환
     * - Retry-After 헤더 설정
     * - 로그는 디버그 레벨로 출력 (과다 방지)
     * </p>
     */
    private void handleRateLimitExceeded(final HttpServletResponse response,
                                         final ConsumptionProbe probe,
                                         final String rateLimitKey) throws IOException {
        final long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000L;

        log.debug("Rate limit exceeded for {}. Retry in {}s", rateLimitKey, retryAfterSeconds);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(UTF_8.name());
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(retryAfterSeconds));

        BaseResponse<Void> baseResponse = BaseResponse.error(TOO_MANY_REQUESTS);

        objectMapper.writeValue(response.getWriter(), baseResponse);
    }

}
