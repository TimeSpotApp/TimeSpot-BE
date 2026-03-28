package com.timespot.backend.domain.user.constant;

import static com.timespot.backend.common.response.ErrorCode.USER_NOTIFICATION_INVALID_TYPE;
import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.annotation.JsonValue;
import com.timespot.backend.common.error.GlobalException;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.user.constant
 * FileName    : NotificationType
 * Author      : loadingKKamo21
 * Date        : 26. 3. 27.
 * Description : 알림 타입 열거형 (회원 알림 설정 - 기기 동기화 지원)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 27.    loadingKKamo21       Initial creation
 */
@Getter
@RequiredArgsConstructor(access = PRIVATE)
public enum NotificationType {

    /**
     * 열차 출발 시간 (기본 알림 - 항상 활성화, 수정 불가)
     */
    DEPARTURE_TIME("DEPARTURE_TIME", "열차 출발 시간", false),

    /**
     * 출발 5 분 전 알림
     */
    DEPARTURE_5_MIN_BEFORE("DEPARTURE_5_MIN_BEFORE", "출발 5 분 전", true),

    /**
     * 출발 10 분 전 알림
     */
    DEPARTURE_10_MIN_BEFORE("DEPARTURE_10_MIN_BEFORE", "출발 10 분 전", true),

    /**
     * 출발 15 분 전 알림
     */
    DEPARTURE_15_MIN_BEFORE("DEPARTURE_15_MIN_BEFORE", "출발 15 분 전", true);

    private final String  code;
    private final String  description;
    private final Boolean isEditable;

    /**
     * 코드 값으로 알림 타입 변환
     *
     * @param code 알림 타입 코드
     * @return 알림 타입 열거형
     * @throws GlobalException 지원하지 않는 알림 타입
     */
    public static NotificationType fromCode(final String code) {
        return Arrays.stream(values())
                     .filter(type -> type.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new GlobalException(USER_NOTIFICATION_INVALID_TYPE));
    }

    /**
     * JSON 직렬화 시 코드 값 반환
     *
     * @return 알림 타입 코드
     */
    @JsonValue
    public String getCode() {
        return code;
    }

}
