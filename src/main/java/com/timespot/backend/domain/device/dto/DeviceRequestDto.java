package com.timespot.backend.domain.device.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static lombok.AccessLevel.PRIVATE;

import com.timespot.backend.domain.device.constant.PlatformType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
                              [필수] APNs/FCM 토큰
                              
                              - **iOS**: 64 바이트 16 진수 문자열 (32 글자)
                              - **Android**: FCM 토큰 문자열
                              - 앱 설치 시마다 재발급됨
                              """,
                example = "f5d8c7b6a9e8d7c6b5a4f3e2d1c0b9a8f7e6d5c4b3a2f1e0d9c8b7a6f5e4d3c2",
                requiredMode = REQUIRED
        )
        private String deviceToken;

        @NotNull(message = "플랫폼 정보는 필수입니다.")
        @Schema(
                description = """
                              [필수] 플랫폼 유형
                              
                              - `IOS`: iOS (iPhone, iPad)
                              - `ANDROID`: Android (향후 확장용)
                              """,
                example = "IOS",
                requiredMode = REQUIRED,
                allowableValues = {"IOS", "ANDROID"}
        )
        private PlatformType platform;

        @Schema(
                description = """
                              앱 버전 (선택)
                              
                              - 알림 호환성 버전 체크용
                              - 형식: "major.minor.patch"
                              """,
                example = "1.2.0",
                requiredMode = NOT_REQUIRED
        )
        private String appVersion;

        @Schema(
                description = """
                              디바이스 모델명 (선택)
                              
                              - 예: "iPhone15,3", "iPad14,5"
                              - 통계 및 분석용으로 사용
                              """,
                example = "iPhone15,3",
                requiredMode = NOT_REQUIRED
        )
        private String deviceModel;

    }

}
