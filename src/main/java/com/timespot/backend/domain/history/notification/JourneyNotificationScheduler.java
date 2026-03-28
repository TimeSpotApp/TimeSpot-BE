package com.timespot.backend.domain.history.notification;

import com.timespot.backend.domain.user.model.NotificationTiming;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public interface JourneyNotificationScheduler {

    void schedule(UUID userId,
                  Long historyId,
                  LocalDateTime trainDepartureTime,
                  Set<NotificationTiming> notificationTimings);

    void cancel(Long historyId);
}