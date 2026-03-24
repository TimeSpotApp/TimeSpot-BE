package com.timespot.backend.common.ratelimit.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timespot.backend.common.security.config.TestSecurityConfig;
import com.timespot.backend.common.security.dto.AuthRequestDto.OAuth2LoginRequest;
import com.timespot.backend.common.util.TestRedisUtils;
import com.timespot.backend.infra.config.TestRedisConfig;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * PackageName : com.timespot.backend.common.ratelimit.filter
 * FileName    : RateLimitFilterIntegrationTest
 * Author      : loadingKKamo21
 * Date        : 26. 3. 16.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 16.    loadingKKamo21       Initial creation
 */
@SpringBootTest(webEnvironment = MOCK)
@AutoConfigureMockMvc
@Import({TestSecurityConfig.class, TestRedisConfig.class, TestRedisUtils.class})
class RateLimitFilterIntegrationTest {

    @Autowired
    private MockMvc        mockMvc;
    @Autowired
    private ObjectMapper   objectMapper;
    @Autowired
    private TestRedisUtils testRedisUtils;

    @BeforeEach
    void setUp() {
        testRedisUtils.flushAll();
    }

    @Nested
    @DisplayName("익명 요청 테스트")
    class AnonymousRequestTests {

        @SneakyThrows
        @Test
        @DisplayName("동일 IP로 연속 요청 시 요청 당 토큰이 1개씩 차감")
        void anonymousRequest_consumeTokensProperly() {
            // given
            final String clientIp = "192.168.1.100";
            final String endpoint = "/api/v1/auth/login";

            String requestBody = objectMapper.writeValueAsString(
                    new OAuth2LoginRequest("google", UUID.randomUUID().toString().replace("-", ""))
            );

            // when
            MvcResult firstResult = mockMvc.perform(post(endpoint)
                                                            .header("X-Forwarded-For", clientIp)
                                                            .contentType(APPLICATION_JSON_VALUE)
                                                            .content(requestBody))
                                           .andExpect(status().isBadRequest())
                                           .andDo(print())
                                           .andReturn();
            long firstRemaining = Long.parseLong(firstResult.getResponse().getHeader("X-Rate-Limit-Remaining"));

            MvcResult secondResult = mockMvc.perform(post(endpoint)
                                                             .header("X-Forwarded-For", clientIp)
                                                             .contentType(APPLICATION_JSON_VALUE)
                                                             .content(requestBody))
                                            .andExpect(status().isBadRequest())
                                            .andDo(print())
                                            .andReturn();
            long secondRemaining = Long.parseLong(secondResult.getResponse().getHeader("X-Rate-Limit-Remaining"));

            // then
            assertEquals(secondRemaining, firstRemaining - 1, "요청 당 토큰이 1개씩 차감되어야 합니다.");
        }

    }

}