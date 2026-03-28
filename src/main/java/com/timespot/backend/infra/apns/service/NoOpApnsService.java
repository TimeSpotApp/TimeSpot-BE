package com.timespot.backend.infra.apns.service;

import com.timespot.backend.infra.apns.dto.ApnsRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(prefix="app.apns", name="enabled", havingValue="false", matchIfMissing=true)
public class NoOpApnsService implements ApnsService {

    @Override
    public void sendNotification(final String deviceToken, final ApnsRequestDto requestDto) {
        log.info("[APNS] APNS is disabled. Skip sending notification. title={}, token={}",
                requestDto.title(),
                maskToken(deviceToken));
    }

    private String maskToken(final String token) {
        if (token == null || token.length() < 10) {
            return "invalid";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}