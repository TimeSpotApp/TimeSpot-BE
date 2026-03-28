package com.timespot.backend.domain.device.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
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
 * 26. 3. 28.    loadingKKamo21       응답 데이터 단순화 (userId, deviceToken, isActive)
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(description = "디바이스 도메인 응답 페이로드")
public abstract class DeviceResponseDto {

    @Getter
    @JsonInclude(NON_NULL)
    @Schema(description = "디바이스 등록 응답 페이로드")
    public static class DeviceRegisterResponse {

        @Schema(
                description = "회원 ID",
                example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                accessMode = READ_ONLY
        )
        private final String userId;

        @Schema(
                description = "디바이스 토큰",
                example = "f5d8c7b6a9e8d7c6b5a4f3e2d1c0b9a8f7e6d5c4b3a2f1e0d9c8b7a6f5e4d3c2",
                accessMode = READ_ONLY
        )
        private final String deviceToken;

        @Schema(
                description = "디바이스 활성화 여부",
                example = "true",
                accessMode = READ_ONLY
        )
        private final Boolean isActive;

        @JsonCreator
        public DeviceRegisterResponse(final String userId,
                                      final String deviceToken,
                                      final Boolean isActive) {
            this.userId = userId;
            this.deviceToken = deviceToken;
            this.isActive = isActive != null ? isActive : false;
        }

    }

}
