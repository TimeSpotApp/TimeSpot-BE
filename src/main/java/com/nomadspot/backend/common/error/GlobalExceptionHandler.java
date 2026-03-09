package com.nomadspot.backend.common.error;

import com.nomadspot.backend.common.response.BaseResponse;
import com.nomadspot.backend.common.response.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * PackageName : com.nomadspot.backend.common.error
 * FileName    : GlobalExceptionHandler
 * Author      : loadingKKamo21
 * Date        : 26. 2. 26.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 2. 26.    loadingKKamo21       Initial creation
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException e
    ) {
        log.error("handleMethodArgumentNotValidException: {}", e.getMessage(), e);
        final ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        final String errorMessage = e.getBindingResult()
                                     .getAllErrors()
                                     .stream()
                                     .map(ObjectError::getDefaultMessage)
                                     .collect(Collectors.joining(", "));
        return ResponseEntity.status(errorCode.getStatus())
                             .body(BaseResponse.error(errorCode, errorMessage));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<BaseResponse<Void>> handleBindException(final BindException e) {
        log.error("handleBindException: {}", e.getMessage(), e);
        final ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        final String errorMessage = e.getBindingResult()
                                     .getAllErrors()
                                     .stream()
                                     .map(ObjectError::getDefaultMessage)
                                     .collect(Collectors.joining(", "));
        return ResponseEntity.status(errorCode.getStatus())
                             .body(BaseResponse.error(errorCode, errorMessage));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<Void>> handleMethodArgumentTypeMismatchException(
            final MethodArgumentTypeMismatchException e
    ) {
        log.error("handleMethodArgumentTypeMismatchException: {} for property {}", e.getMessage(), e.getName(), e);
        final ErrorCode errorCode = ErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH;
        final String errorMessage = String.format("요청 인자 '%s'의 타입이 올바르지 않습니다. 예상 타입: %s",
                                                  e.getName(),
                                                  e.getRequiredType() != null ? e.getRequiredType().getName()
                                                                              : "알 수 없음");
        return ResponseEntity.status(errorCode.getStatus())
                             .body(BaseResponse.error(errorCode, errorMessage));
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<BaseResponse<Void>> handleServletRequestBindingException(
            final ServletRequestBindingException e
    ) {
        log.error("handleServletRequestBindingException: {}", e.getMessage(), e);
        final ErrorCode errorCode    = ErrorCode.INVALID_INPUT_VALUE;
        final String    errorMessage = "필수 요청 파라미터 또는 헤더가 누락되었습니다.";
        return ResponseEntity.status(errorCode.getStatus())
                             .body(BaseResponse.error(errorCode, errorMessage));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseResponse<Void>> handleHttpRequestMethodNotSupportedException(
            final HttpRequestMethodNotSupportedException e
    ) {
        log.error("handleHttpRequestMethodNotSupportedException: {}", e.getMessage(), e);
        final ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
        final String supportedMethods = e.getSupportedHttpMethods() != null
                                        ? e.getSupportedHttpMethods()
                                           .stream()
                                           .map(HttpMethod::name)
                                           .collect(Collectors.joining(", "))
                                        : "알 수 없음";
        final String errorMessage = String.format("요청하신 HTTP Method '%s'는 이 리소스에서 지원되지 않습니다. 지원되는 Method: [%s]",
                                                  e.getMethod(),
                                                  supportedMethods);
        return ResponseEntity.status(errorCode.getStatus())
                             .body(BaseResponse.error(errorCode, errorMessage));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<BaseResponse<Void>> handleHttpMediaTypeNotSupportedException(
            final HttpMediaTypeNotSupportedException e
    ) {
        log.error("handleHttpMediaTypeNotSupportedException: {}", e.getMessage(), e);
        final ErrorCode errorCode = ErrorCode.METHOD_NOT_SUPPORTED;
        final String supportedTypes = e.getSupportedMediaTypes() != null
                                      ? e.getSupportedMediaTypes()
                                         .stream()
                                         .map(Object::toString)
                                         .collect(Collectors.joining(", "))
                                      : "알 수 없음";
        final String errorMessage = String.format("요청하신 Content-Type '%s'는 지원되지 않습니다. 지원되는 Content-Type: [%s]",
                                                  e.getContentType(),
                                                  supportedTypes);
        return ResponseEntity.status(errorCode.getStatus())
                             .body(BaseResponse.error(errorCode, errorMessage));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleNoResourceFoundException(final NoResourceFoundException e) {
        log.error("handleNoResourceFoundException: {}", e.getMessage(), e);
        final ErrorCode errorCode = ErrorCode.RESOURCE_NOT_FOUND;
        return ResponseEntity.status(errorCode.getStatus())
                             .body(BaseResponse.error(errorCode));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleEntityNotFoundException(final EntityNotFoundException e) {
        log.error("handleEntityNotFoundException: {}", e.getMessage(), e);
        final ErrorCode errorCode = ErrorCode.ENTITY_NOT_FOUND;
        return ResponseEntity.status(errorCode.getStatus())
                             .body(BaseResponse.error(errorCode));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleDataIntegrityViolationException(
            final DataIntegrityViolationException e
    ) {
        final ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getStatus())
                             .body(BaseResponse.error(errorCode));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccessDeniedException(final AccessDeniedException e) {
        log.error("handleAccessDeniedException: {}", e.getMessage(), e);
        final ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
        return ResponseEntity.status(errorCode.getStatus())
                             .body(BaseResponse.error(errorCode));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<Void>> handleAuthenticationException(final AuthenticationException e) {
        log.error("handleAuthenticationException: {}", e.getMessage(), e);
        final ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(BaseResponse.error(errorCode));
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<BaseResponse<Void>> handleGlobalException(final GlobalException e) {
        log.error("handleCustomException: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getErrorCode().getStatus())
                             .body(BaseResponse.error(e.getErrorCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleException(final Exception e) {
        log.error("handleException: {}", e.getMessage(), e);
        final ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getStatus())
                             .body(BaseResponse.error(errorCode));
    }

}
