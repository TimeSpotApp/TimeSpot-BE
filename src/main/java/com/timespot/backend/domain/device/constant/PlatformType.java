package com.timespot.backend.domain.device.constant;

import static com.timespot.backend.common.response.ErrorCode.DEVICE_INVALID_PLATFORM;

import com.timespot.backend.common.error.GlobalException;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.device.constant
 * FileName    : PlatformType
 * Author      : loadingKKamo21
 * Date        : 26. 3. 27.
 * Description : 디바이스 플랫폼 타입 열거형
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 27.    loadingKKamo21       Initial creation
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum PlatformType {

    /**
     * iOS 플랫폼 (iPhone, iPad)
     */
    IOS("IOS", "iOS"),

    /**
     * Android 플랫폼 (향후 확장용)
     */
    ANDROID("ANDROID", "Android");

    private final String code;
    private final String description;

    /**
     * 코드 값으로 플랫폼 타입 변환
     *
     * @param code 플랫폼 코드
     * @return 플랫폼 타입 열거형
     * @throws GlobalException 지원하지 않는 플랫폼
     */
    public static PlatformType fromCode(final String code) {
        return Arrays.stream(values())
                     .filter(type -> type.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new GlobalException(DEVICE_INVALID_PLATFORM));
    }

    /**
     * JSON 직렬화 시 코드 값 반환
     *
     * @return 플랫폼 코드
     */
    @JsonValue
    public String getCode() {
        return code;
    }

}
