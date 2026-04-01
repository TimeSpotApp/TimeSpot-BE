package com.timespot.backend.domain.history.notification;

import static com.timespot.backend.domain.place.constant.PlaceConst.PLATFORM_WAIT_TIME;

import com.timespot.backend.domain.device.dao.ApnsTokenRepository;
import com.timespot.backend.domain.user.model.NotificationTiming;
import com.timespot.backend.infra.apns.dto.ApnsRequestDto;
import com.timespot.backend.infra.apns.service.ApnsService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

/**
 * PackageName : com.timespot.backend.domain.history.notification
 * FileName    : ApnsJourneyNotificationScheduler
 * Author      : 이승현
 * Date        : 26. 3. 28.
 * Description : APNs 여정 알림 스케줄러 구현체
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 28.    이승현                Initial creation
 * 26. 4. 1.     loadingKKamo21       walkTimeFromPlace 와 platformWaitTime 을 고려한 알림 발송 로직 추가
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApnsJourneyNotificationScheduler implements JourneyNotificationScheduler {

    private static final String TITLE                                  = "출발 알림";
    private static final String CUSTOM_PAYLOAD_KEY_NOTIFICATION_SCHEMA = "notificationSchema";

    private final TaskScheduler                                 taskScheduler;
    private final ApnsService                                   apnsService;
    private final ApnsTokenRepository                           apnsTokenRepository;
    private final Clock                                         clock;
    private final ConcurrentMap<Long, List<ScheduledFuture<?>>> scheduledTasksByHistoryId = new ConcurrentHashMap<>();

    @Override
    public void schedule(final UUID userId,
                         final Long historyId,
                         final LocalDateTime trainDepartureTime,
                         final int walkTimeFromPlace,
                         final Set<NotificationTiming> notificationTimings) {
        Set<NotificationTiming> effectiveTimings = notificationTimings == null
                                                   ? new HashSet<>()
                                                   : new HashSet<>(notificationTimings);
        effectiveTimings.add(NotificationTiming.DEPARTURE_TIME);
        effectiveTimings.add(NotificationTiming.END_JOURNEY);
        effectiveTimings.remove(NotificationTiming.NONE);

        final LocalDateTime            now              = LocalDateTime.now(clock);
        final List<ScheduledFuture<?>> scheduledFutures = new ArrayList<>();

        effectiveTimings.forEach(timing -> {
            LocalDateTime notifyAt = calculateNotificationTime(trainDepartureTime, walkTimeFromPlace, timing);

            if (!notifyAt.isAfter(now)) {
                log.info("[NOTIFICATION] Skip past timing. userId={}, historyId={}, timing={}, notifyAt={}",
                         userId, historyId, timing, notifyAt);
                return;
            }

            Instant triggerAt = notifyAt.atZone(clock.getZone()).toInstant();

            ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(
                    () -> sendApnsToActiveTokens(userId, historyId, timing),
                    triggerAt
            );
            if (scheduledFuture != null) {
                scheduledFutures.add(scheduledFuture);
            }

            log.info("[NOTIFICATION] Scheduled notification. userId={}, historyId={}, timing={}, notifyAt={}",
                     userId, historyId, timing, notifyAt);
        });

        if (!scheduledFutures.isEmpty()) {
            scheduledTasksByHistoryId.put(historyId, scheduledFutures);
        }
    }

    @Override
    public void cancel(final Long historyId) {
        List<ScheduledFuture<?>> scheduledFutures = scheduledTasksByHistoryId.remove(historyId);
        if (scheduledFutures == null || scheduledFutures.isEmpty()) {
            return;
        }

        scheduledFutures.forEach(future -> future.cancel(false));
        log.info("[NOTIFICATION] Canceled scheduled notifications. historyId={}, taskCount={}",
                 historyId, scheduledFutures.size());
    }

    private void sendApnsToActiveTokens(final UUID userId,
                                        final Long historyId,
                                        final NotificationTiming timing) {
        List<String> deviceTokens = apnsTokenRepository.findActiveApnsTokensByUserId(userId);
        if (deviceTokens.isEmpty()) {
            log.info("[NOTIFICATION] No active APNS token at send time. userId={}, historyId={}, timing={}",
                     userId, historyId, timing);
            return;
        }

        deviceTokens.forEach(deviceToken -> sendApns(deviceToken, historyId, timing));
    }

    private void sendApns(final String deviceToken, final Long historyId, final NotificationTiming timing) {
        try {
            ApnsRequestDto requestDto = new ApnsRequestDto(
                    TITLE,
                    timing.getMessage(),
                    1,
                    Map.of(
                            "historyId", historyId,
                            "timing", timing.toValue(),
                            CUSTOM_PAYLOAD_KEY_NOTIFICATION_SCHEMA, timing.toSchema()
                    )
            );

            apnsService.sendNotification(deviceToken, requestDto);
        } catch (Exception e) {
            log.error("[NOTIFICATION] Failed to send APNS. historyId={}, timing={}", historyId, timing, e);
        }
    }

    /**
     * 알림 발송 시간 계산
     * DEPARTURE_TIME, BEFORE_*: 장소 출발 시간 기준 (trainDepartureTime - walkTimeFromPlace - platformWaitTime)
     * END_JOURNEY: 열차 출발 시간 기준 (trainDepartureTime)
     */
    private LocalDateTime calculateNotificationTime(final LocalDateTime trainDepartureTime,
                                                    final int walkTimeFromPlace,
                                                    final NotificationTiming timing) {
        if (timing == NotificationTiming.END_JOURNEY) return trainDepartureTime;

        int           totalBufferTime    = walkTimeFromPlace + PLATFORM_WAIT_TIME;
        LocalDateTime placeDepartureTime = trainDepartureTime.minusMinutes(totalBufferTime);

        return placeDepartureTime.minusMinutes(timing.getBeforeMinutes());
    }

}
