package com.timespot.backend.domain.device.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static lombok.AccessLevel.PRIVATE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.device.dto
 * FileName    : DeviceRequestDto
 * Author      : loadingKKamo21
 * Date        : 26. 3. 27.
 * Description : 디바이스 도메인 요청 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 27.    loadingKKamo21       Initial creation
 * 26. 3. 28.    loadingKKamo21       디바이스 토큰 단일 파라미터로 단순화
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(description = "디바이스 도메인 요청 페이로드")
public abstract class DeviceRequestDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "디바이스 등록 요청 페이로드")
    public static class DeviceRegisterRequest {

        @NotBlank(message = "디바이스 토큰은 필수입니다.")
        @Schema(
                description = """
                              [필수] 디바이스 푸시 알림 토큰
                              
                              - **iOS**: APNs 토큰 (64 바이트 16 진수 문자열)
                              - **Android**: FCM 토큰
                              - 앱 재설치 시마다 재발급됨
                              """,
                example = "f5d8c7b6a9e8d7c6b5a4f3e2d1c0b9a8f7e6d5c4b3a2f1e0d9c8b7a6f5e4d3c2",
                requiredMode = REQUIRED
        )
        private String deviceToken;

    }

}
