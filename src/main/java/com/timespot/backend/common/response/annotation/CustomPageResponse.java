package com.timespot.backend.common.response.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PackageName : com.timespot.backend.common.response.annotation
 * FileName    : CustomPageResponse
 * Author      : loadingKKamo21
 * Date        : 26. 3. 5.
 * Description : 커스텀 페이지 응답 필드 지정 애너테이션
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 5.     loadingKKamo21       Initial creation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomPageResponse {

    boolean content() default true;

    boolean totalElements() default true;

    boolean totalPages() default true;

    boolean size() default true;

    boolean number() default true;

    boolean numberOfElements() default true;

    boolean sort() default true;

    boolean empty() default true;

    boolean hasContent() default true;

    boolean first() default true;

    boolean last() default true;

    boolean hasPrevious() default true;

    boolean hasNext() default true;

}
