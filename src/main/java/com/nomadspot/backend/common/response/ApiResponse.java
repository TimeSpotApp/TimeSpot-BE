package com.nomadspot.backend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * PackageName : com.nomadspot.backend.common.response
 * FileName    : ApiResponse
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
public class ApiResponse<T> {

    @NotNull
    private final Integer code;
    @NotBlank
    private final String  message;
    @JsonInclude(Include.NON_NULL)
    private final T       data;

    public static <T> ApiResponse<T> of(final HttpStatus status, final String message) {
        return ApiResponse.<T>builder()
                          .code(status.value())
                          .message(message)
                          .build();
    }

    public static <T> ApiResponse<T> of(final HttpStatus status, final String message, final T data) {
        return ApiResponse.<T>builder()
                          .code(status.value())
                          .message(message)
                          .data(data)
                          .build();
    }

    public static <T> ApiResponse<T> success(final SuccessCode successCode) {
        return ApiResponse.<T>builder()
                          .code(successCode.getStatus().value())
                          .message(successCode.getMessage())
                          .build();
    }

    public static <T> ApiResponse<T> success(final SuccessCode successCode, final T data) {
        return ApiResponse.<T>builder()
                          .code(successCode.getStatus().value())
                          .message(successCode.getMessage())
                          .data(data).build();
    }

    public static <T> ApiResponse<T> error(final ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                          .code(errorCode.getStatus().value())
                          .message(errorCode.getMessage())
                          .build();
    }

    public static <T> ApiResponse<T> error(final ErrorCode errorCode, final String message) {
        return ApiResponse.<T>builder()
                          .code(errorCode.getStatus().value())
                          .message(message)
                          .build();
    }

    public static <T> ApiResponse<T> error(final ErrorCode errorCode, final String message, final T data) {
        return ApiResponse.<T>builder()
                          .code(errorCode.getStatus().value())
                          .message(message)
                          .data(data)
                          .build();
    }

}
