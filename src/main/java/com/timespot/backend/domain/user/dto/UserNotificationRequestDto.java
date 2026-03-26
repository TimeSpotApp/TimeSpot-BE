package com.timespot.backend.domain.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static lombok.AccessLevel.PRIVATE;

import com.timespot.backend.domain.user.constant.NotificationType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.user.dto
 * FileName    : UserNotificationRequestDto
 * Author      : loadingKKamo21
 * Date        : 26. 3. 27.
 * Description : 사용자 알림 설정 DTO (회원 전용, 기기 동기화 지원)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 27.    loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(description = "사용자 알림 설정 DTO (회원 전용)")
public abstract class UserNotificationRequestDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "알림 설정 수정 요청 페이로드")
    public static class NotificationSettingsRequest {

        @NotNull(message = "알림 설정 목록은 필수입니다.")
        @Size(min = 1, max = 3, message = "알림 설정은 1 개 이상 3 개 이하입니다.")
        @ArraySchema(
                minItems = 1,
                maxItems = 3,
                arraySchema = @Schema(
                        description = """
                                      [필수] 알림 설정 목록
                                      
                                      - `DEPARTURE_TIME` 은 기본 알림이므로 제외 (항상 활성화)
                                      - 나머지 3 가지 (5 분/10 분/15 분 전) 중 선택
                                      - 중복 활성화 가능
                                      - 모두 비활성화 시 "설정 안 함"으로 간주
                                      - **회원 전용**: 로그인 시 모든 기기에서 동기화됨
                                      """,
                        example = """
                                  [
                                    {"type": "DEPARTURE_5_MIN_BEFORE", "isEnabled": true},
                                    {"type": "DEPARTURE_10_MIN_BEFORE", "isEnabled": true},
                                    {"type": "DEPARTURE_15_MIN_BEFORE", "isEnabled": false}
                                  ]
                                  """
                )
        )
        @Schema(
                description = "알림 설정 목록 (최대 3 개)",
                requiredMode = REQUIRED
        )
        private List<NotificationSettingItem> notificationSettings;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "개별 알림 설정 항목")
    public static class NotificationSettingItem {

        @NotNull(message = "알림 타입은 필수입니다.")
        @Schema(
                description = """
                              [필수] 알림 타입
                              
                              - `DEPARTURE_5_MIN_BEFORE`: 출발 5 분 전
                              - `DEPARTURE_10_MIN_BEFORE`: 출발 10 분 전
                              - `DEPARTURE_15_MIN_BEFORE`: 출발 15 분 전
                              - `DEPARTURE_TIME` 은 기본 알림이므로 제외
                              """,
                example = "DEPARTURE_5_MIN_BEFORE",
                requiredMode = REQUIRED,
                allowableValues = {
                        "DEPARTURE_5_MIN_BEFORE",
                        "DEPARTURE_10_MIN_BEFORE",
                        "DEPARTURE_15_MIN_BEFORE"
                }
        )
        private NotificationType type;

        @NotNull(message = "활성화 여부는 필수입니다.")
        @Schema(
                description = """
                              [필수] 활성화 여부
                              
                              - `true`: 해당 알림 수신
                              - `false`: 해당 알림 미수신
                              """,
                example = "true",
                requiredMode = REQUIRED
        )
        private Boolean isEnabled;

    }

}
