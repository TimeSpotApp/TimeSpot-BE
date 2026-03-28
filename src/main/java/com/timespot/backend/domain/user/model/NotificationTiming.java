package com.timespot.backend.domain.user.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.response.ErrorCode;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationTiming {

    NONE("none", null, "none"),
    DEPARTURE_TIME("departure_time", 0, "지금 바로 출발해야 해요!"),
    BEFORE_5_MINUTES("before_5_minutes", 5, "5분 뒤면 역으로 슬슬 일어날 채비를 할 시간이에요."),
    BEFORE_10_MINUTES("before_10_minutes", 10, "10분 뒤면 역으로 출발해야 해요!"),
    BEFORE_15_MINUTES("before_15_minutes", 15, "역으로 출발하기까지 15분 남았어요!");

    private final String code;
    private final Integer beforeMinutes;
    private final String message;

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
}