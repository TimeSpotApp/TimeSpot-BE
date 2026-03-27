package com.timespot.backend.domain.device.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import com.timespot.backend.domain.device.constant.PlatformType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.device.dto
 * FileName    : DeviceResponseDto
 * Author      : loadingKKamo21
 * Date        : 26. 3. 27.
 * Description : 디바이스 도메인 응답 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 27.    loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(description = "디바이스 도메인 응답 페이로드")
public abstract class DeviceResponseDto {

    @Getter
    @JsonInclude(NON_NULL)
    @Schema(description = "디바이스 등록 응답 페이로드")
    public static class DeviceRegisterResponse {

        @Schema(
                description = "디바이스 등록 ID",
                example = "12345",
                accessMode = READ_ONLY
        )
        private final Long deviceId;

        @Schema(
                description = "회원 ID (비회원은 null)",
                example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                accessMode = READ_ONLY
        )
        private final String userId;

        @Schema(
                description = "디바이스 토큰 (일부 마스킹)",
                example = "f5d8c7b6...e4d3c2",
                accessMode = READ_ONLY
        )
        private final String deviceToken;

        @Schema(
                description = "플랫폼 유형",
                example = "IOS",
                accessMode = READ_ONLY,
                allowableValues = {"IOS", "ANDROID"}
        )
        private final PlatformType platform;

        @Schema(
                description = "활성화 여부",
                example = "true",
                accessMode = READ_ONLY
        )
        private final Boolean isActive;

        @Schema(
                description = "등록 일시 (ISO-8601 형식)",
                example = "2024-01-15T10:30:00",
                accessMode = READ_ONLY
        )
        private final LocalDateTime createdAt;

        @QueryProjection
        @JsonCreator
        public DeviceRegisterResponse(final Long deviceId,
                                      final String userId,
                                      final String deviceToken,
                                      final PlatformType platform,
                                      final Boolean isActive,
                                      final LocalDateTime createdAt) {
            this.deviceId = deviceId;
            this.userId = userId;
            this.deviceToken = deviceToken != null && deviceToken.length() > 16
                               ? deviceToken.substring(0, 8) + "..." + deviceToken.substring(deviceToken.length() - 8)
                               : deviceToken;
            this.platform = platform;
            this.isActive = isActive != null ? isActive : false;
            this.createdAt = createdAt;
        }

    }

}
