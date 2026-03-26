package com.timespot.backend.domain.user.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import com.timespot.backend.domain.user.model.MapApi;
import com.timespot.backend.domain.user.model.ProviderType;
import com.timespot.backend.domain.user.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.user.dto
 * FileName    : UserResponseDto
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description : 사용자 도메인 응답 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 * 26. 3. 24.    loadingKKamo21       API 문서 상세화 (예시 값 추가)
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(description = "회원 도메인 응답 페이로드")
public abstract class UserResponseDto {

    @Getter
    @JsonInclude(NON_NULL)
    @Schema(description = "회원 정보 응답 페이로드")
    public static class UserInfoResponse {

        @Schema(
                description = "회원 ID (UUID 형식)",
                example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                accessMode = READ_ONLY
        )
        private final String userId;

        @Schema(
                description = "이메일 주소",
                example = "user@example.com",
                accessMode = READ_ONLY
        )
        private final String email;

        @Schema(
                description = "닉네임",
                example = "홍길동",
                accessMode = READ_ONLY
        )
        private final String nickname;

        @Schema(
                description = "주사용 지도 API 유형",
                example = "GOOGLE",
                accessMode = READ_ONLY
        )
        private final String mapApi;

        @Schema(
                description = "계정 역할 (USER, ADMIN)",
                example = "USER",
                accessMode = READ_ONLY
        )
        private final String role;

        @Schema(
                description = "소셜 인증 제공자 유형",
                example = "APPLE",
                accessMode = READ_ONLY
        )
        private final String providerType;

        @Schema(
                description = "총 방문 횟수 (여정 횟수)",
                example = "15",
                accessMode = READ_ONLY
        )
        private final Integer totalVisitCount;

        @Schema(
                description = "총 여정 누적 시간 (분)",
                example = "450",
                accessMode = READ_ONLY
        )
        private final Integer totalJourneyMinutes;

        @Schema(
                description = "가입 일시 (ISO-8601 형식)",
                example = "2024-01-15T10:30:00",
                accessMode = READ_ONLY
        )
        private final LocalDateTime createdAt;

        @QueryProjection
        @JsonCreator
        public UserInfoResponse(final UUID userId,
                                final String email,
                                final String nickname,
                                final MapApi mapApi,
                                final UserRole role,
                                final ProviderType providerType,
                                final Integer totalVisitCount,
                                final Integer totalJourneyMinutes,
                                final LocalDateTime createdAt) {
            this.userId = userId != null ? userId.toString() : null;
            this.email = email;
            this.nickname = nickname;
            this.mapApi = mapApi != null ? mapApi.name() : null;
            this.role = role != null ? role.name() : null;
            this.providerType = providerType != null ? providerType.name() : null;
            this.totalVisitCount = totalVisitCount != null ? totalVisitCount : 0;
            this.totalJourneyMinutes = totalJourneyMinutes != null ? totalJourneyMinutes : 0;
            this.createdAt = createdAt;
        }

    }

    @Getter
    @Schema(description = "알림 설정 응답 페이로드")
    public static class UserNotificationResponse {

        @Schema(description = "선택된 알림 시간 설정 목록")
        private final List<String> notificationTimings;

        public UserNotificationResponse(final List<String> notificationTimings) {
            this.notificationTimings = notificationTimings;
        }
    }
}
