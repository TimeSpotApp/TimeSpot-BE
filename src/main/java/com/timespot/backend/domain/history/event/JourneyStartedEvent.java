package com.timespot.backend.domain.history.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record JourneyStartedEvent(
        UUID userId,
        Long historyId,
        LocalDateTime trainDepartureTime
) {
}