package com.timespot.backend.domain.history.event;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.response.ErrorCode;
import com.timespot.backend.domain.history.notification.JourneyNotificationScheduler;
import com.timespot.backend.domain.user.dao.UserRepository;
import com.timespot.backend.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * PackageName : com.timespot.backend.domain.history.event
 * FileName    : JourneyStartedEventHandler
 * Author      : 이승현
 * Date        : 26. 3. 28.
 * Description : 여정 시작 이벤트 핸들러
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 28.    이승현                Initial creation
 * 26. 4. 1.     loadingKKamo21       walkTimeFromPlace 파라미터 추가
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JourneyStartedEventHandler {

    private final UserRepository               userRepository;
    private final JourneyNotificationScheduler journeyNotificationScheduler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleJourneyStarted(final JourneyStartedEvent event) {
        User user = userRepository.findById(event.userId())
                                  .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        journeyNotificationScheduler.schedule(
                event.userId(),
                event.historyId(),
                event.trainDepartureTime(),
                event.walkTimeFromPlace(),
                user.getNotificationTimings()
        );

        log.info("[NOTIFICATION] Journey event handled. userId={}, historyId={}, walkTimeFromPlace={}",
                 event.userId(), event.historyId(), event.walkTimeFromPlace());
    }
}
