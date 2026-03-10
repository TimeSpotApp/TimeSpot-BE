package com.nomadspot.backend.common.response.advice;

import com.nomadspot.backend.common.response.BaseResponse;
import com.nomadspot.backend.common.response.annotation.CustomPageResponse;
import com.nomadspot.backend.common.response.util.PageResponseConverter;
import java.util.Map;
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
 * PackageName : com.nomadspot.backend.common.response.advice
 * FileName    : PageResponseAdvice
 * Author      : loadingKKamo21
 * Date        : 26. 3. 5.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 5.     loadingKKamo21       Initial creation
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

        Page<?>         page         = null;
        BaseResponse<?> baseResponse = null;

        if (body instanceof Page) page = (Page<?>) body;
        else if (body instanceof BaseResponse) {
            baseResponse = (BaseResponse<?>) body;
            if (baseResponse.getData() instanceof Page) page = (Page<?>) baseResponse.getData();
        }

        if (page == null) return body;

        Map<String, Object> customPageResponse = PageResponseConverter.convertPageToCustomMap(page, annotation);

        if (body instanceof Page) return customPageResponse;
        else return BaseResponse.of(HttpStatus.valueOf(baseResponse.getCode()),
                                    baseResponse.getMessage(),
                                    customPageResponse);
    }

}
