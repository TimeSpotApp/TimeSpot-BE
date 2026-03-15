package com.timespot.backend.common.ratelimit.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timespot.backend.common.ratelimit.builder.RateLimitBucketBuilder;
import com.timespot.backend.common.ratelimit.config.RateLimitConfig;
import com.timespot.backend.common.ratelimit.constant.RateLimitConst;
import com.timespot.backend.common.ratelimit.filter.RateLimitFilterTest.TestController;
import com.timespot.backend.common.ratelimit.properties.RateLimitProperties;
import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.ErrorCode;
import com.timespot.backend.common.response.SuccessCode;
import com.timespot.backend.common.security.config.TestSecurityConfig;
import com.timespot.backend.common.security.config.annotation.CustomWithMockUser;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.proxy.RemoteBucketBuilder;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.timespot.backend.common.ratelimit.filter
 * FileName    : RateLimitFilterTest
 * Author      : loadingKKamo21
 * Date        : 26. 3. 15.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 15.    loadingKKamo21       Initial creation
 */
@WebMvcTest(controllers = TestController.class)
@Import({TestSecurityConfig.class, RateLimitConfig.class, RateLimitBucketBuilder.class, RateLimitProperties.class})
class RateLimitFilterTest {

    @Autowired
    private MockMvc      mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProxyManager<String>        proxyManager;
    @Mock
    private RemoteBucketBuilder<String> remoteBucketBuilder;
    @Mock
    private BucketProxy                 bucketProxy;

    @BeforeEach
    void setUp() {
        given(proxyManager.builder()).willReturn(remoteBucketBuilder);
        given(remoteBucketBuilder.build(anyString(), any(Supplier.class))).willReturn(bucketProxy);
    }

    @RestController
    static class TestController {

        @GetMapping("/api/v1/test")
        public ResponseEntity<BaseResponse<Void>> test() {
            return ResponseEntity.ok(BaseResponse.success(SuccessCode.REQUEST_SUCCESS));
        }

        @GetMapping({"/management", "/swagger-ui", "/v3/api-docs"})
        public ResponseEntity<BaseResponse<Void>> excluded() {
            return ResponseEntity.ok(BaseResponse.success(SuccessCode.REQUEST_SUCCESS));
        }

    }

    @Nested
    @DisplayName("인증된 요청 테스트")
    class AuthenticatedRequestTests {

        @Test
        @CustomWithMockUser
        @DisplayName("인증된 사용자 요청 Rate Limit 적용, 토큰 소비 성공")
        void authenticatedRequest_rateLimitSuccess() throws Exception {
            // given
            ConsumptionProbe probe = mock(ConsumptionProbe.class);

            given(probe.isConsumed()).willReturn(true);
            given(probe.getRemainingTokens()).willReturn(9L);
            given(bucketProxy.tryConsumeAndReturnRemaining(eq(1L))).willReturn(probe);

            // when
            ResultActions resultActions = mockMvc.perform(get("/api/v1/test"));

            // then
            final BaseResponse<Void> baseResponse = BaseResponse.success(SuccessCode.REQUEST_SUCCESS);
            final String             responseBody = objectMapper.writeValueAsString(baseResponse);

            resultActions.andExpect(status().isOk())
                         .andExpect(header().exists("X-Rate-Limit-Remaining"))
                         .andExpect(header().string("X-Rate-Limit-Remaining", "9"))
                         .andExpect(content().json(responseBody))
                         .andDo(print());
        }

        @Test
        @CustomWithMockUser
        @DisplayName("인증된 사용자 요청 Rate Limit 적용, 토큰 소진 시 429 응답")
        void authenticatedRequest_rateLimitExceeded() throws Exception {
            // given
            ConsumptionProbe probe = mock(ConsumptionProbe.class);

            given(probe.isConsumed()).willReturn(false);
            given(probe.getNanosToWaitForRefill()).willReturn(60_000_000_000L);
            given(bucketProxy.tryConsumeAndReturnRemaining(eq(1L))).willReturn(probe);

            // when
            ResultActions resultActions = mockMvc.perform(get("/api/v1/test"));

            // then
            final BaseResponse<Void> baseResponse = BaseResponse.error(ErrorCode.TOO_MANY_REQUESTS);
            final String             responseBody = objectMapper.writeValueAsString(baseResponse);

            resultActions.andExpect(status().isTooManyRequests())
                         .andExpect(header().exists("Retry-After"))
                         .andExpect(header().string("Retry-After", "60"))
                         .andExpect(content().json(responseBody))
                         .andDo(print());
        }

    }

    @Nested
    @DisplayName("익명 요청 테스트")
    class AnonymousRequestTests {

        static Stream<Arguments> ipHeaderProvider() {
            return Stream.of(
                    Arguments.of("X-Forwarded-For", "192.168.1.100"),
                    Arguments.of("X-Real-IP", "321.123.321.123")
            );
        }

        @Test
        @DisplayName("익명 사용자 요청 Rate Limit 적용, 토큰 소비 성공")
        void anonymousRequest_rateLimitSuccess() throws Exception {
            // given
            ConsumptionProbe probe = mock(ConsumptionProbe.class);

            given(probe.isConsumed()).willReturn(true);
            given(probe.getRemainingTokens()).willReturn(4L);
            given(bucketProxy.tryConsumeAndReturnRemaining(eq(1L))).willReturn(probe);

            // when
            ResultActions resultActions = mockMvc.perform(get("/api/v1/test"));

            // then
            final BaseResponse<Void> baseResponse = BaseResponse.success(SuccessCode.REQUEST_SUCCESS);
            final String             responseBody = objectMapper.writeValueAsString(baseResponse);

            resultActions.andExpect(status().isOk())
                         .andExpect(header().exists("X-Rate-Limit-Remaining"))
                         .andExpect(header().string("X-Rate-Limit-Remaining", "4"))
                         .andExpect(content().json(responseBody))
                         .andDo(print());
        }

        @Test
        @DisplayName("익명 사용자 요청 Rate Limit 적용, 토큰 소진 시 429 응답")
        void anonymousRequest_rateLimitExceeded() throws Exception {
            // given
            ConsumptionProbe probe = mock(ConsumptionProbe.class);

            given(probe.isConsumed()).willReturn(false);
            given(probe.getNanosToWaitForRefill()).willReturn(60_000_000_000L);
            given(bucketProxy.tryConsumeAndReturnRemaining(eq(1L))).willReturn(probe);

            // when
            ResultActions resultActions = mockMvc.perform(get("/api/v1/test"));

            // then
            final BaseResponse<Void> baseResponse = BaseResponse.error(ErrorCode.TOO_MANY_REQUESTS);
            final String             responseBody = objectMapper.writeValueAsString(baseResponse);

            resultActions.andExpect(status().isTooManyRequests())
                         .andExpect(header().exists("Retry-After"))
                         .andExpect(header().string("Retry-After", "60"))
                         .andExpect(content().json(responseBody))
                         .andDo(print());
        }

        @ParameterizedTest
        @MethodSource("ipHeaderProvider")
        @DisplayName("익명 사용자 요청 시 다양한 IP 헤더에서 정확한 IP 추출")
        void anonymousRequest_extractsIpFromVariousHeaders(final String headerName, final String ipValue)
        throws Exception {
            // given
            ConsumptionProbe probe = mock(ConsumptionProbe.class);

            given(probe.isConsumed()).willReturn(true);
            given(probe.getRemainingTokens()).willReturn(4L);
            given(bucketProxy.tryConsumeAndReturnRemaining(eq(1L))).willReturn(probe);

            // when
            mockMvc.perform(get("/api/v1/test").header(headerName, ipValue))
                   .andExpect(status().isOk())
                   .andDo(print());

            // then
            verify(proxyManager.builder())
                    .build(eq(RateLimitConst.ANONYMOUS_KEY_PREFIX + ipValue), any(Supplier.class));
        }

    }

    @Nested
    @DisplayName("필터링 제외 URL 테스트")
    class ExcludedPathTests {

        static Stream<String> excludedPathPrefixesProvider() {
            return Stream.of(RateLimitConst.excludedPathPrefixes);
        }

        @ParameterizedTest
        @MethodSource("excludedPathPrefixesProvider")
        @DisplayName("제외된 URL 요청 Rate Limit 미적용")
        void excludedPath(final String excludedPathPrefix) throws Exception {
            // when
            ResultActions resultActions = mockMvc.perform(get(excludedPathPrefix));

            // then
            resultActions.andExpect(status().isOk())
                         .andExpect(header().doesNotExist("X-Rate-Limit-Remaining"))
                         .andDo(print());

            verify(proxyManager, never()).builder();
        }

    }

}
