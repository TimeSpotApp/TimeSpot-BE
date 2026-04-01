package com.timespot.backend.domain.history.notification;

import com.timespot.backend.domain.user.model.NotificationTiming;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * PackageName : com.timespot.backend.domain.history.notification
 * FileName    : JourneyNotificationScheduler
 * Author      : 이승현
 * Date        : 26. 3. 28.
 * Description : 여정 알림 스케줄러 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 28.    이승현                Initial creation
 * 26. 4. 1.     loadingKKamo21       walkTimeFromPlace 파라미터 추가
 */
public interface JourneyNotificationScheduler {

    void schedule(UUID userId,
                  Long historyId,
                  LocalDateTime trainDepartureTime,
                  int walkTimeFromPlace,
                  Set<NotificationTiming> notificationTimings);

    void cancel(Long historyId);
}
