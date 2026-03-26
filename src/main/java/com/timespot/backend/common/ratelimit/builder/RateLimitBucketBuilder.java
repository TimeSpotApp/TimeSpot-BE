package com.timespot.backend.common.ratelimit.builder;

import static com.timespot.backend.common.ratelimit.constant.RateLimitConst.REFILL_STRATEGY_FIXED;
import static com.timespot.backend.common.ratelimit.constant.RateLimitConst.REFILL_STRATEGY_GREEDY;

import com.timespot.backend.common.ratelimit.properties.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * PackageName : com.timespot.backend.common.ratelimit.builder
 * FileName    : RateLimitBucketBuilder
 * Author      : loadingKKamo21
 * Date        : 26. 3. 14.
 * Description : Rate Limiting 버킷 설정 빌더 (엔드포인트별 BucketConfiguration 생성)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 14.    loadingKKamo21       Initial creation
 */
@Component
@RequiredArgsConstructor
public class RateLimitBucketBuilder {

    private final RateLimitProperties rateLimitProperties;

    @Getter
    private Map<String, BucketConfiguration> endpointConfigs;
    @Getter
    private BucketConfiguration              defaultConfig;
    //@Getter
    //private BucketConfiguration              authenticatedConfig;   // NOTE: 인증된 사용자 요청에 대한 설정은 추후 필요 시 활용
    @Getter
    private BucketConfiguration              anonymousConfig;

    // ========================= 내부 메서드 =========================

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
            case REFILL_STRATEGY_GREEDY -> Bandwidth.builder()
                                                    .capacity(capacity)
                                                    .refillGreedy(capacity, Duration.ofMinutes(durationMinutes))
                                                    .build();
            case REFILL_STRATEGY_FIXED -> Bandwidth.builder()
                                                   .capacity(capacity)
                                                   .refillIntervally(capacity, Duration.ofMinutes(durationMinutes))
                                                   .build();
            default -> throw new IllegalArgumentException("Invalid refill strategy: " + refillStrategy);
        };
    }

    @PostConstruct
    private void init() {
        this.endpointConfigs = rateLimitProperties.getEndpoints()
                                                  .stream()
                                                  .collect(
                                                          Collectors.collectingAndThen(
                                                                  Collectors.toMap(
                                                                          RateLimitProperties.EndpointConfig::getPathPattern,
                                                                          config -> buildBucketConfiguration(
                                                                                  config.getCapacity(),
                                                                                  config.getDurationMinutes(),
                                                                                  config.getRefillStrategy()
                                                                          )
                                                                  ),
                                                                  Collections::unmodifiableMap
                                                          )
                                                  );
        this.defaultConfig = buildBucketConfiguration(
                rateLimitProperties.getDefaultConfig().getCapacity(),
                rateLimitProperties.getDefaultConfig().getDurationMinutes(),
                rateLimitProperties.getDefaultConfig().getRefillStrategy()
        );
        //this.authenticatedConfig = buildBucketConfiguration(
        //        rateLimitProperties.getAuthenticated().getCapacity(),
        //        rateLimitProperties.getAuthenticated().getDurationMinutes(),
        //        rateLimitProperties.getAuthenticated().getRefillStrategy()
        //);
        this.anonymousConfig = buildBucketConfiguration(
                rateLimitProperties.getAnonymous().getCapacity(),
                rateLimitProperties.getAnonymous().getDurationMinutes(),
                rateLimitProperties.getAnonymous().getRefillStrategy()
        );
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
