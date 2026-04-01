package com.timespot.backend.domain.user.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.response.ErrorCode;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.user.model
 * FileName    : NotificationTiming
 * Author      : 이승현
 * Date        : 26. 3. 8.
 * Description : 사용자 알림 시간 설정 Enum
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3.28.     이승현                Initial creation
 * 26. 4. 1.     loadingKKamo21       END_JOURNEY 추가 (여정 종료 알림)
 */
@Getter
@RequiredArgsConstructor
public enum NotificationTiming {

    NONE("none", null, "none", "none"),
    DEPARTURE_TIME("departure_time", 0, "지금 바로 출발해야 해요!", "departure_time"),
    BEFORE_5_MINUTES("before_5_minutes", 5, "5 분 뒤면 역으로 슬슬 일어날 채비를 할 시간이에요.", "5_min_before"),
    BEFORE_10_MINUTES("before_10_minutes", 10, "10 분 뒤면 역으로 출발해야 해요!", "10_min_before"),
    BEFORE_15_MINUTES("before_15_minutes", 15, "역으로 출발하기까지 15 분 남았어요!", "15_min_before"),
    END_JOURNEY("end_journey", null, "이번 여정은 어떠셨나요? 열차에 잘 탑승하셨나요?", "end_journey");

    private static final String NOTIFICATION_SCHEMA_PREFIX = "timespot://";

    private final String  code;
    private final Integer beforeMinutes;
    private final String  message;
    private final String  schemaPath;

    @JsonCreator
    public static NotificationTiming from(final String value) {
        if (value == null || value.isBlank()) {
            throw new GlobalException(ErrorCode.USER_NOTIFICATION_TIMING_NOT_SUPPORTED);
        }

        return Arrays.stream(values())
                     .filter(type -> type.name().equalsIgnoreCase(value) || type.code.equalsIgnoreCase(value))
                     .findFirst()
                     .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOTIFICATION_TIMING_NOT_SUPPORTED));
    }

    @JsonValue
    public String toValue() {
        return this.code;
    }

    public String toSchema() {
        return NOTIFICATION_SCHEMA_PREFIX + schemaPath;
    }
}
