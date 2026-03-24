package com.timespot.backend.infra.apns.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Map;

@Builder
@Schema(description = "APNS 알림 요청 DTO")
public record ApnsRequestDto(
        @NotBlank
        @Schema(description = "알림 제목", example = "기차 시간이 임박했어요!")
        String title,

        @NotBlank
        @Schema(description = "알림 본문", example = "기차 출발까지 5분 남았어요!")
        String body,

        @NotNull
        @Schema(description = "앱 아이콘에 표시될 배지 카운트", example = "1")
        Integer badge,

        @Schema(description = "알림과 함께 전달할 커스텀 데이터")
        Map<String, Object> customPayload
) {
}
