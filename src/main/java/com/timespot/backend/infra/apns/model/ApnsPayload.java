package com.timespot.backend.infra.apns.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApnsPayload(
        Aps aps,
        Map<String, Object> customPayload
) {
    public static ApnsPayload of(String title, String body, int badge, Map<String, Object> customPayload) {
        Alert alert = Alert.builder()
                .title(title)
                .body(body)
                .build();

        Aps aps = Aps.builder()
                .alert(alert)
                .badge(badge)
                .sound("default")
                .build();

        return ApnsPayload.builder()
                .aps(aps)
                .customPayload(customPayload)
                .build();
    }

    @Builder
    public record Aps(
            Alert alert,
            Integer badge,
            String sound
    ) {
    }

    @Builder
    public record Alert(
            String title,
            String body
    ) {
    }
}
