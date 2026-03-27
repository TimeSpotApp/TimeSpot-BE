package com.timespot.backend.domain.user.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.timespot.backend.domain.user.constant.NotificationType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.user.dto
 * FileName    : UserNotificationResponseDto
 * Author      : loadingKKamo21
 * Date        : 26. 3. 27.
 * Description : 사용자 알림 설정 응답 DTO (회원 전용, 기기 동기화 지원)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 27.    loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(description = "사용자 알림 설정 응답 DTO (회원 전용)")
public abstract class UserNotificationResponseDto {

    @Getter
    @JsonInclude(NON_NULL)
    @Schema(description = "알림 설정 조회 응답 페이로드")
    public static class NotificationSettingsResponse {

        @ArraySchema(
                arraySchema = @Schema(
                        description = """
                                      알림 설정 목록
                                      
                                      - `DEPARTURE_TIME`: 열차 출발 시간 (기본 - 항상 활성화)
                                      - `DEPARTURE_5_MIN_BEFORE`: 출발 5 분 전
                                      - `DEPARTURE_10_MIN_BEFORE`: 출발 10 분 전
                                      - `DEPARTURE_15_MIN_BEFORE`: 출발 15 분 전
                                      
                                      **회원 전용**: 로그인 시 모든 기기에서 동기화됨
                                      """,
                        example = """
                                  [
                                    {"type": "DEPARTURE_TIME", "isEnabled": true, "isEditable": false},
                                    {"type": "DEPARTURE_5_MIN_BEFORE", "isEnabled": true, "isEditable": true},
                                    {"type": "DEPARTURE_10_MIN_BEFORE", "isEnabled": true, "isEditable": true},
                                    {"type": "DEPARTURE_15_MIN_BEFORE", "isEnabled": false, "isEditable": true}
                                  ]
                                  """
                )
        )
        @Schema(
                description = "알림 설정 목록 (4 가지 타입 모두 반환)",
                accessMode = READ_ONLY
        )
        private final List<NotificationSettingItem> settings;

        @Schema(
                description = "최종 수정 일시 (ISO-8601 형식)",
                example = "2024-01-15T10:30:00",
                accessMode = READ_ONLY
        )
        private final LocalDateTime updatedAt;

        @JsonCreator
        public NotificationSettingsResponse(final List<NotificationSettingItem> settings,
                                            final LocalDateTime updatedAt) {
            this.settings = settings;
            this.updatedAt = updatedAt;
        }

    }

    @Getter
    @RequiredArgsConstructor
    @JsonInclude(NON_NULL)
    @Schema(description = "개별 알림 설정 항목 응답")
    public static class NotificationSettingItem {

        @Schema(
                description = "알림 타입",
                example = "DEPARTURE_5_MIN_BEFORE",
                accessMode = READ_ONLY,
                allowableValues = {
                        "DEPARTURE_TIME",
                        "DEPARTURE_5_MIN_BEFORE",
                        "DEPARTURE_10_MIN_BEFORE",
                        "DEPARTURE_15_MIN_BEFORE"
                }
        )
        private final NotificationType type;

        @Schema(
                description = "활성화 여부",
                example = "true",
                accessMode = READ_ONLY
        )
        private final Boolean isEnabled;

        @Schema(
                description = """
                              수정 가능 여부
                              
                              - `false`: `DEPARTURE_TIME` (기본 알림, 수정 불가)
                              - `true`: 나머지 알림 (사용자 설정 가능)
                              """,
                example = "true",
                accessMode = READ_ONLY
        )
        private final Boolean isEditable;

    }

}
