package com.timespot.backend.domain.history.notification;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApnsJourneyNotificationScheduler implements JourneyNotificationScheduler {

    private static final String TITLE = "출발 알림";

    private final TaskScheduler taskScheduler;
    private final ApnsService apnsService;
    private final ApnsTokenRepository apnsTokenRepository;
    private final Clock clock;
    private final ConcurrentMap<Long, List<ScheduledFuture<?>>> scheduledTasksByHistoryId = new ConcurrentHashMap<>();

    @Override
    public void schedule(final java.util.UUID userId,
                         final Long historyId,
                         final LocalDateTime trainDepartureTime,
                         final Set<NotificationTiming> notificationTimings) {
        Set<NotificationTiming> effectiveTimings = notificationTimings == null
                ? new HashSet<>()
                : new HashSet<>(notificationTimings);
        effectiveTimings.add(NotificationTiming.DEPARTURE_TIME);
        effectiveTimings.remove(NotificationTiming.NONE);

        final LocalDateTime now = LocalDateTime.now(clock);
        final List<ScheduledFuture<?>> scheduledFutures = new ArrayList<>();

        effectiveTimings.forEach(timing -> {
            LocalDateTime notifyAt = trainDepartureTime.minusMinutes(timing.getBeforeMinutes());

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

    private void sendApnsToActiveTokens(final java.util.UUID userId,
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
                            "timing", timing.toValue()
                    )
            );

            apnsService.sendNotification(deviceToken, requestDto);
        } catch (Exception e) {
            log.error("[NOTIFICATION] Failed to send APNS. historyId={}, timing={}", historyId, timing, e);
        }
    }
}