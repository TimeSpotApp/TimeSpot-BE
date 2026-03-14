package com.timespot.backend.common.ratelimit.builder;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class RateLimitBucketBuilder {

    private final Map<String, BucketConfiguration> configurations = new HashMap<>();

    /**
     * API 경로별 버킷 설정 추가
     *
     * @param pathPattern API 경로 패턴 (prefix 매칭)
     * @param capacity    토큰 수
     * @param duration    리필 기간
     */
    public void addConfig(final String pathPattern, final long capacity, final Duration duration) {
        BucketConfiguration config = createBucketConfiguration(capacity, duration, false);
        configurations.put(pathPattern, config);
        log.info("Rate limit config added: {} -> {} requests per {}", pathPattern, capacity, duration);
    }

    /**
     * 여러 API 경로에 동일한 설정 일괄 추가
     *
     * @param pathPatterns API 경로 패턴 목록
     * @param capacity     토큰 수
     * @param duration     리필 기간
     */
    public void addConfigs(final List<String> pathPatterns, final long capacity, final Duration duration) {
        BucketConfiguration config = createBucketConfiguration(capacity, duration, false);
        for (String pathPattern : pathPatterns) {
            configurations.put(pathPattern, config);
            log.info("Rate limit config added: {} -> {} requests per {}", pathPattern, capacity, duration);
        }
    }

    /**
     * 여러 API 경로에 동일한 설정 일괄 추가
     *
     * @param capacity     토큰 수
     * @param duration     리필 기간
     * @param pathPatterns API 경로 패턴 목록
     */
    public void addConfigs(final long capacity, final Duration duration, final String... pathPatterns) {
        BucketConfiguration config = createBucketConfiguration(capacity, duration, false);
        for (String pathPattern : pathPatterns) {
            configurations.put(pathPattern, config);
            log.info("Rate limit config added: {} -> {} requests per {}", pathPattern, capacity, duration);
        }
    }

    /**
     * API 경로별 버킷 설정 추가 (고정 인터벌 리필)
     *
     * @param pathPattern API 경로 패턴 (prefix 매칭)
     * @param capacity    토큰 수
     * @param duration    리필 기간
     */
    public void addConfigWithFixedRefill(final String pathPattern, final long capacity, final Duration duration) {
        BucketConfiguration config = createBucketConfiguration(capacity, duration, true);
        configurations.put(pathPattern, config);
        log.info("Rate limit config added: {} -> {} requests per {}", pathPattern, capacity, duration);
    }

    /**
     * 여러 API 경로에 동일한 설정 일괄 추가 (고정 인터벌 리필)
     *
     * @param pathPatterns API 경로 패턴 목록
     * @param capacity     토큰 수
     * @param duration     리필 기간
     */
    public void addConfigsWithFixedRefill(final List<String> pathPatterns,
                                          final long capacity,
                                          final Duration duration) {
        BucketConfiguration config = createBucketConfiguration(capacity, duration, true);
        for (String pathPattern : pathPatterns) {
            configurations.put(pathPattern, config);
            log.info("Rate limit config added: {} -> {} requests per {}", pathPattern, capacity, duration);
        }
    }

    /**
     * 여러 API 경로에 동일한 설정 일괄 추가 (고정 인터벌 리필)
     *
     * @param capacity     토큰 수
     * @param duration     리필 기간
     * @param pathPatterns API 경로 패턴 목록
     */
    public void addConfigsWithFixedRefill(final long capacity, final Duration duration, final String... pathPatterns) {
        BucketConfiguration config = createBucketConfiguration(capacity, duration, true);
        for (String pathPattern : pathPatterns) {
            configurations.put(pathPattern, config);
            log.info("Rate limit config added: {} -> {} requests per {}", pathPattern, capacity, duration);
        }
    }

    /**
     * 모든 설정 반환
     *
     * @return API 경로별 버킷 설정 맵
     */
    public Map<String, BucketConfiguration> getConfigurations() {
        return Collections.unmodifiableMap(configurations);
    }

    /**
     * 기본 버킷 설정 반환
     *
     * @return 1분 당 100 요청 (greedy 리필)
     */
    public BucketConfiguration getDefaultConfig() {
        return BucketConfiguration.builder()
                                  .addLimit(Bandwidth.builder()
                                                     .capacity(100)
                                                     .refillGreedy(100, Duration.ofMinutes(1))
                                                     .build())
                                  .build();
    }

    /**
     * 특정 경로 설정 찾기 (prefix 매칭)
     *
     * @param requestURI 요청 URI
     * @return 매칭된 버킷 설정, 없으면 null
     */
    public BucketConfiguration findMatchingConfig(final String requestURI) {
        return configurations.entrySet()
                             .stream()
                             .filter(entry -> requestURI.startsWith(entry.getKey()))
                             .max(Map.Entry.comparingByKey())
                             .map(Map.Entry::getValue)
                             .orElse(null);
    }

    // ========================= 내부 메서드 =========================

    /**
     * 버킷 설정 생성
     *
     * @param capacity    토큰 수
     * @param duration    리필 기간
     * @param fixedRefill 고정 인터벌 리필 여부
     * @return 버킷 설정
     */
    private BucketConfiguration createBucketConfiguration(final long capacity,
                                                          final Duration duration,
                                                          final boolean fixedRefill) {
        return BucketConfiguration.builder()
                                  .addLimit(fixedRefill
                                            ? Bandwidth.builder()
                                                       .capacity(capacity)
                                                       .refillIntervally(capacity, duration)
                                                       .build()
                                            : Bandwidth.builder()
                                                       .capacity(capacity)
                                                       .refillGreedy(capacity, duration)
                                                       .build())
                                  .build();
    }

}
