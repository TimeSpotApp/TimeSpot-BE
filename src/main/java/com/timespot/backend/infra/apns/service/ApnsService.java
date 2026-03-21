package com.timespot.backend.infra.apns.service;

import com.timespot.backend.infra.apns.dto.ApnsRequestDto;

public interface ApnsService {

    /**
     * APNS를 통해 푸시 알림을 비동기적으로 전송합니다.
     *
     * @param deviceToken  알림을 받을 디바이스 토큰
     * @param requestDto   알림 내용을 담은 DTO
     */
    void sendNotification(String deviceToken, ApnsRequestDto requestDto);
}
