package com.timespot.backend.infra.apns.service;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timespot.backend.domain.device.dao.ApnsTokenRepository;
import com.timespot.backend.infra.apns.config.properties.ApnsProperties;
import com.timespot.backend.infra.apns.dto.ApnsRequestDto;
import com.timespot.backend.infra.apns.model.ApnsPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.apns", name = "private-key")
public class ApnsServiceImpl implements ApnsService {

    private final ApnsClient apnsClient;
    private final ApnsProperties apnsProperties;
    private final ObjectMapper objectMapper;
    private final ApnsTokenRepository apnsTokenRepository;

    @Override
    public void sendNotification(String deviceToken, ApnsRequestDto requestDto) {
        log.info("[APNS] Sending notification to device token: {}", maskToken(deviceToken));

        try {
            // 1. APNS 페이로드 생성
            ApnsPayload payload = ApnsPayload.of(
                    requestDto.title(),
                    requestDto.body(),
                    requestDto.badge(),
                    requestDto.customPayload()
            );
            String payloadString = objectMapper.writeValueAsString(payload);

            // 2. Pushy 라이브러리를 사용하여 알림 객체 생성
            SimpleApnsPushNotification notification = new SimpleApnsPushNotification(
                    deviceToken,
                    apnsProperties.getBundleId(), // "topic"
                    payloadString
            );

            // 3. 비동기적으로 알림 전송
            CompletableFuture<PushNotificationResponse<SimpleApnsPushNotification>> sendFuture =
                    apnsClient.sendNotification(notification);

            // 4. 비동기 결과 처리
            handleResponse(deviceToken, sendFuture);

        } catch (JsonProcessingException e) {
            log.error("[APNS] Failed to serialize APNS payload. Request: {}", requestDto, e);
        } catch (Exception e) {
            log.error("[APNS] Unexpected error while preparing APNS notification.", e);
        }
    }

    /**
     * APNS 응답을 비동기적으로 처리하고 로그를 남깁니다.
     *
     * @param sendFuture APNS 전송 결과 CompletableFuture
     */
        private void handleResponse(final String deviceToken, final CompletableFuture<PushNotificationResponse<SimpleApnsPushNotification>> sendFuture) {
        sendFuture.whenComplete((response, cause) -> {
            if (response != null) {
                // 알림이 APNS에 의해 수락되었을 경우
                if (response.isAccepted()) {
                    log.info("[APNS] Notification accepted by APNS. Push notification: {}", response.getPushNotification());
                } else {
                    // 알림이 APNS에 의해 거부되었을 경우
                    log.warn("[APNS] Notification rejected by APNS. Reason: {}", response.getRejectionReason());
                    response.getTokenInvalidationTimestamp().ifPresent(timestamp -> {
                        log.warn("[APNS] ...and the token is invalid as of {}", timestamp);
                        invalidateToken(deviceToken, timestamp);
                    });
                }
            } else {
                // APNS로 전송 중 예외가 발생했을 경우
                log.error("[APNS] Failed to send notification to APNS.", cause);
            }
        });
    }

    private void invalidateToken(final String deviceToken, final Instant invalidatedAt) {
        int updatedCount = apnsTokenRepository.invalidateToken(deviceToken);
        if (updatedCount > 0) {
            log.info("[APNS] Token invalidated. token={}, invalidatedAt={}", maskToken(deviceToken), invalidatedAt);
        }
    }

    private String maskToken(final String token) {
        if (token == null || token.length() < 10) {
            return "invalid";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}

