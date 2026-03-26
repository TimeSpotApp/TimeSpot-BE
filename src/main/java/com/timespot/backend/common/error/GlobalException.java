package com.timespot.backend.common.error;

import com.timespot.backend.common.response.ErrorCode;
import lombok.Getter;

/**
 * PackageName : com.timespot.backend.common.error
 * FileName    : GlobalException
 * Author      : loadingKKamo21
 * Date        : 26. 2. 26.
 * Description : 전역 커스텀 예외 클래스 (ErrorCode 기반)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 2. 26.    loadingKKamo21       Initial creation
 */
@Getter
public class GlobalException extends RuntimeException {

    private final ErrorCode errorCode;

    public GlobalException(final ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public GlobalException(final ErrorCode errorCode, final Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public GlobalException(final ErrorCode errorCode, final String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public GlobalException(final ErrorCode errorCode, final String message, final Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}
