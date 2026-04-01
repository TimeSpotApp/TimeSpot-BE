package com.timespot.backend.domain.history.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PackageName : com.timespot.backend.domain.history.event
 * FileName    : JourneyStartedEvent
 * Author      : 이승현
 * Date        : 26. 3. 28.
 * Description : 여정 시작 이벤트 (알림 예약 트리거)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 28.    이승현                Initial creation
 * 26. 4. 1.     loadingKKamo21       walkTimeFromPlace 필드 추가 (장소→역 도보 시간)
 */
public record JourneyStartedEvent(
        UUID userId,
        Long historyId,
        LocalDateTime trainDepartureTime,
        int walkTimeFromPlace
) {
}
