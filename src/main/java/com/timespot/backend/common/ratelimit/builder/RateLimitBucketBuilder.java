package com.timespot.backend.common.ratelimit.builder;

import com.timespot.backend.common.ratelimit.constant.RateLimitConst;
import com.timespot.backend.common.ratelimit.properties.RateLimitProperties;
import com.timespot.backend.common.ratelimit.properties.RateLimitProperties.BucketConfig;
import com.timespot.backend.common.ratelimit.properties.RateLimitProperties.EndpointConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * PackageName : com.timespot.backend.common.ratelimit.builder
 * FileName    : RateLimitBucketBuilder
 * Author      : loadingKKamo21
 * Date        : 26. 3. 14.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 14.    loadingKKamo21       Initial creation
 */
@Component
@RequiredArgsConstructor
public class RateLimitBucketBuilder {

    private final RateLimitProperties rateLimitProperties;

    /**
     * Bandwidth 생성
     *
     * @param capacity        토큰 수
     * @param durationMinutes 리필 주기(분)
     * @param refillStrategy  리필 전략
     * @return Bandwidth
     */
    private static Bandwidth createBandwidth(long capacity, long durationMinutes, final String refillStrategy) {
        return switch (refillStrategy) {
            case RateLimitConst.REFILL_STRATEGY_GREEDY -> Bandwidth.builder()
                                                                   .capacity(capacity)
                                                                   .refillGreedy(capacity,
                                                                                 Duration.ofMinutes(durationMinutes))
                                                                   .build();
            case RateLimitConst.REFILL_STRATEGY_FIXED -> Bandwidth.builder()
                                                                  .capacity(capacity)
                                                                  .refillIntervally(capacity,
                                                                                    Duration.ofMinutes(durationMinutes))
                                                                  .build();
            default -> throw new IllegalArgumentException("Invalid refill strategy: " + refillStrategy);
        };
    }

    /**
     * 기본 버킷 설정
     *
     * @return BucketConfiguration
     */
    public BucketConfiguration getDefaultConfig() {
        BucketConfig config = rateLimitProperties.getDefaultConfig();
        return buildBucketConfiguration(config.getCapacity(), config.getDurationMinutes(), config.getRefillStrategy());
    }

    /**
     * 익명 요청 버킷 설정 (IP 기반)
     *
     * @return BucketConfiguration
     */
    public BucketConfiguration getAnonymousConfig() {
        BucketConfig config = rateLimitProperties.getAnonymous();
        return buildBucketConfiguration(config.getCapacity(), config.getDurationMinutes(), config.getRefillStrategy());
    }

    /**
     * 인증된 클라이언트 요청 버킷 설정 (UserId 기반)
     *
     * @return BucketConfiguration
     */
    public BucketConfiguration getAuthenticatedConfig() {
        BucketConfig config = rateLimitProperties.getAuthenticated();
        return buildBucketConfiguration(config.getCapacity(), config.getDurationMinutes(), config.getRefillStrategy());
    }

    // ========================= 내부 메서드 =========================

    /**
     * 엔드포인트 별 버킷 설정 맵
     *
     * @return {경로패턴: BucketConfiguration} 맵
     */
    public Map<String, BucketConfiguration> getConfigurations() {
        return rateLimitProperties.getEndpoints()
                                  .stream()
                                  .collect(Collectors.toMap(
                                          EndpointConfig::getPathPattern,
                                          config -> buildBucketConfiguration(
                                                  config.getCapacity(),
                                                  config.getDurationMinutes(),
                                                  config.getRefillStrategy()
                                          ),
                                          (existing, replacement) -> existing,
                                          HashMap::new
                                  ));
    }

    /**
     * 버킷 설정 빌드
     *
     * @param capacity        토큰 수
     * @param durationMinutes 리필 주기(분)
     * @param refillStrategy  리필 전략
     * @return 버킷 설정
     */
    private BucketConfiguration buildBucketConfiguration(final long capacity,
                                                         final long durationMinutes,
                                                         final String refillStrategy) {
        return BucketConfiguration.builder()
                                  .addLimit(createBandwidth(capacity, durationMinutes, refillStrategy))
                                  .build();
    }

}
