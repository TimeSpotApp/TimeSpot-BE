package com.timespot.backend.common.response.advice;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.annotation.CustomPageResponse;
import com.timespot.backend.common.response.util.PageResponseConverter;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * PackageName : com.timespot.backend.common.response.advice
 * FileName    : PageResponseAdvice
 * Author      : loadingKKamo21
 * Date        : 26. 3. 5.
 * Description : 페이지 응답 자동 변환 Advice (@CustomPageResponse 처리)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 5.     loadingKKamo21       Initial creation
 * 26. 3. 26.    loadingKKamo21       재귀적 Page 변환 로직 추가
 */
@RestControllerAdvice
public class PageResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.hasMethodAnnotation(CustomPageResponse.class);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        CustomPageResponse annotation = returnType.getMethodAnnotation(CustomPageResponse.class);
        if (annotation == null) return body;

        if (body instanceof Page) return PageResponseConverter.convertPageToCustomMap((Page<?>) body, annotation);

        if (body instanceof BaseResponse) {
            BaseResponse<?> baseResponse = (BaseResponse<?>) body;
            Object          data         = baseResponse.getData();

            Object convertedData = PageResponseConverter.convertNestedPageToObject(data, annotation);

            return BaseResponse.of(HttpStatus.valueOf(baseResponse.getCode()),
                                   baseResponse.getMessage(),
                                   convertedData);
        }

        return body;
    }

}
