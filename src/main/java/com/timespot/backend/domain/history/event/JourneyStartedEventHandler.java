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

@Slf4j
@Component
@RequiredArgsConstructor
public class JourneyStartedEventHandler {

    private final UserRepository userRepository;
    private final JourneyNotificationScheduler journeyNotificationScheduler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleJourneyStarted(final JourneyStartedEvent event) {
        User user = userRepository.findById(event.userId())
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        journeyNotificationScheduler.schedule(
                event.userId(),
                event.historyId(),
                event.trainDepartureTime(),
                user.getNotificationTimings()
        );

        log.info("[NOTIFICATION] Journey event handled. userId={}, historyId={}", event.userId(), event.historyId());
    }
}