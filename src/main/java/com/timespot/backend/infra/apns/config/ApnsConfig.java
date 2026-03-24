package com.timespot.backend.infra.apns.config;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.auth.ApnsSigningKey;
import com.timespot.backend.infra.apns.config.properties.ApnsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ApnsProperties.class)
public class ApnsConfig {

    private final ApnsProperties apnsProperties;

    /**
     * ApnsClient를 Spring Bean으로 등록합니다.
     *
     * Pushy의 ApnsClient는 재사용 가능하고 스레드에 안전하므로,
     * 애플리케이션 전체에서 단일 인스턴스를 유지하는 것이 효율적입니다.
     *
     * @return ApnsClient 싱글톤 인스턴스
     * @throws IOException              p8 인증서 파일을 읽는 중 오류가 발생할 경우
     * @throws NoSuchAlgorithmException 시스템에서 필요한 암호화 알고리즘을 지원하지 않을 경우
     * @throws InvalidKeyException      p8 인증서 파일의 형식이 유효하지 않을 경우
     */
    @Bean
    public ApnsClient apnsClient() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        return new ApnsClientBuilder()
                .setApnsServer(apnsProperties.getEndpoint())
                .setSigningKey(ApnsSigningKey.loadFromInputStream(
                        new ByteArrayInputStream(normalizePrivateKey(apnsProperties.getPrivateKey()).getBytes(StandardCharsets.UTF_8)),
                        apnsProperties.getTeamId(),
                        apnsProperties.getKeyId()))
                .build();
    }

    private String normalizePrivateKey(final String privateKey) {
        return privateKey.replace("\\n", "\n");
    }
}
