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

    /** 응답 데이터 내용 포함 여부 */
    boolean content() default true;

    /** 전체 요소 개수 포함 여부 */
    boolean totalElements() default true;

    /** 전체 페이지 수 포함 여부 */
    boolean totalPages() default true;

    /** 페이지 크기 포함 여부 */
    boolean size() default true;

    /** 현재 페이지 번호 포함 여부 */
    boolean number() default true;

    /** 현재 페이지 요소 개수 포함 여부 */
    boolean numberOfElements() default true;

    /** 정렬 정보 포함 여부 */
    boolean sort() default true;

    /** 빈 페이지 여부 포함 여부 */
    boolean empty() default true;

    /** 콘텐츠 포함 여부 포함 여부 */
    boolean hasContent() default true;

    /** 첫 페이지 여부 포함 여부 */
    boolean first() default true;

    /** 마지막 페이지 여부 포함 여부 */
    boolean last() default true;

    /** 이전 페이지 존재 여부 포함 여부 */
    boolean hasPrevious() default true;

    /** 다음 페이지 존재 여부 포함 여부 */
    boolean hasNext() default true;

}
