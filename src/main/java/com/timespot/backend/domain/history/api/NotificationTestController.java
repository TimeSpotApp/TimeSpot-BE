package com.timespot.backend.domain.history.api;

import static com.timespot.backend.common.response.SuccessCode.REQUEST_SUCCESS;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.device.dao.ApnsTokenRepository;
import com.timespot.backend.domain.user.model.NotificationTiming;
import com.timespot.backend.infra.apns.dto.ApnsRequestDto;
import com.timespot.backend.infra.apns.service.ApnsService;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.timespot.backend.domain.history.api
 * FileName    : NotificationTestController
 * Author      : loadingKKamo21
 * Date        : 26. 3. 28.
 * Description : 알림 테스트 컨트롤러 (개발/디버깅 전용)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 28.    이승현                Initial creation
 * 26. 3. 28.    loadingKKamo21       ApiDocs 추가
 */
@RestController
@RequestMapping("/api/v1/test/notifications")
@RequiredArgsConstructor
public class NotificationTestController implements NotificationTestApiDocs {

    private static final String TITLE = "테스트 알림";
    private static final String CUSTOM_PAYLOAD_KEY_NOTIFICATION_SCHEMA = "notificationSchema";

    private final ApnsTokenRepository apnsTokenRepository;
    private final ApnsService apnsService;

    @Override
    @PostMapping("/departure-time")
    public ResponseEntity<BaseResponse<NotificationTestResponse>> testDepartureTime(
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        NotificationTestResponse responseData = sendTestNotification(userDetails.getId(), NotificationTiming.DEPARTURE_TIME);
        return ResponseEntity.ok(BaseResponse.success(REQUEST_SUCCESS, responseData));
    }

    @Override
    @PostMapping("/5-min-before")
    public ResponseEntity<BaseResponse<NotificationTestResponse>> test5MinBefore(
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        NotificationTestResponse responseData = sendTestNotification(userDetails.getId(), NotificationTiming.BEFORE_5_MINUTES);
        return ResponseEntity.ok(BaseResponse.success(REQUEST_SUCCESS, responseData));
    }

    @Override
    @PostMapping("/10-min-before")
    public ResponseEntity<BaseResponse<NotificationTestResponse>> test10MinBefore(
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        NotificationTestResponse responseData = sendTestNotification(userDetails.getId(), NotificationTiming.BEFORE_10_MINUTES);
        return ResponseEntity.ok(BaseResponse.success(REQUEST_SUCCESS, responseData));
    }

    @Override
    @PostMapping("/15-min-before")
    public ResponseEntity<BaseResponse<NotificationTestResponse>> test15MinBefore(
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        NotificationTestResponse responseData = sendTestNotification(userDetails.getId(), NotificationTiming.BEFORE_15_MINUTES);
        return ResponseEntity.ok(BaseResponse.success(REQUEST_SUCCESS, responseData));
    }

    private NotificationTestResponse sendTestNotification(final java.util.UUID userId, final NotificationTiming timing) {
        List<String> deviceTokens = apnsTokenRepository.findActiveApnsTokensByUserId(userId);

        ApnsRequestDto requestDto = new ApnsRequestDto(
                TITLE,
                timing.getMessage(),
                1,
                Map.of(
                        "timing", timing.toValue(),
                        CUSTOM_PAYLOAD_KEY_NOTIFICATION_SCHEMA, timing.toSchema(),
                        "isTest", true
                )
        );

        deviceTokens.forEach(deviceToken -> apnsService.sendNotification(deviceToken, requestDto));

        return NotificationTestResponse.builder()
                .notificationType(timing.name())
                .notificationSchema(timing.toSchema())
                .sentDeviceCount(deviceTokens.size())
                .title(requestDto.title())
                .body(requestDto.body())
                .badge(requestDto.badge())
                .customPayload(requestDto.customPayload())
                .build();
    }

    @Builder
    public record NotificationTestResponse(
            String notificationType,
            String notificationSchema,
            Integer sentDeviceCount,
            String title,
            String body,
            Integer badge,
            Map<String, Object> customPayload
    ) {
    }
}
