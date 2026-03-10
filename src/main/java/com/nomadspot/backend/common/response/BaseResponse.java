package com.nomadspot.backend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * PackageName : com.nomadspot.backend.common.response
 * FileName    : BaseResponse
 * Author      : loadingKKamo21
 * Date        : 26. 2. 26.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 2. 26.    loadingKKamo21       Initial creation
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@JsonInclude(Include.NON_NULL)
@Schema(description = "API 응답 페이로드")
public class BaseResponse<T> {

    @NotNull
    @Schema(description = "HTTP 상태 코드")
    private final Integer code;
    @NotBlank
    @Schema(description = "응답 메시지")
    private final String  message;
    @Schema(description = "응답 데이터", nullable = true)
    private final T       data;

    public static <T> BaseResponse<T> of(final HttpStatus status, final String message) {
        return BaseResponse.<T>builder()
                           .code(status.value())
                           .message(message)
                           .build();
    }

    public static <T> BaseResponse<T> of(final HttpStatus status, final String message, final T data) {
        return BaseResponse.<T>builder()
                           .code(status.value())
                           .message(message)
                           .data(data)
                           .build();
    }

    public static <T> BaseResponse<T> success(final SuccessCode successCode) {
        return BaseResponse.<T>builder()
                           .code(successCode.getStatus().value())
                           .message(successCode.getMessage())
                           .build();
    }

    public static <T> BaseResponse<T> success(final SuccessCode successCode, final T data) {
        return BaseResponse.<T>builder()
                           .code(successCode.getStatus().value())
                           .message(successCode.getMessage())
                           .data(data).build();
    }

    public static <T> BaseResponse<T> error(final ErrorCode errorCode) {
        return BaseResponse.<T>builder()
                           .code(errorCode.getStatus().value())
                           .message(errorCode.getMessage())
                           .build();
    }

    public static <T> BaseResponse<T> error(final ErrorCode errorCode, final String message) {
        return BaseResponse.<T>builder()
                           .code(errorCode.getStatus().value())
                           .message(message)
                           .build();
    }

    public static <T> BaseResponse<T> error(final ErrorCode errorCode, final String message, final T data) {
        return BaseResponse.<T>builder()
                           .code(errorCode.getStatus().value())
                           .message(message)
                           .data(data)
                           .build();
    }

}
