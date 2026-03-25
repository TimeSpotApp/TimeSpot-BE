package com.timespot.backend.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * PackageName : com.timespot.backend.common.util
 * FileName    : SortUtils
 * Author      : loadingKKamo21
 * Date        : 26. 3. 26.
 * Description : 정렬 유틸리티 클래스 (다중 정렬 지원)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 26.    loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class SortUtils {

    /**
     * Pageable 객체 생성 (다중 정렬 지원)
     *
     * @param page 페이지 번호 (1 부터 시작)
     * @param size 페이지 크기
     * @param sort 정렬 문자열 (프로퍼티,방향 - 쉼표로 여러 개 지정 가능)
     * @return Pageable 객체
     */
    public static Pageable createPageable(final int page, final int size, final String sort) {
        return PageRequest.of(page - 1, size, createSort(sort));
    }

    /**
     * 다중 정렬 Sort 객체 생성
     * <p>
     * 정렬 문자열 형식: "property1,direction1,property2,direction2,..."
     * </p>
     *
     * @param sort 정렬 문자열 (프로퍼티,방향 - 쉼표로 여러 개 지정 가능)
     * @return Sort 객체
     * @throws IllegalArgumentException 정렬 형식이 올바르지 않은 경우
     */
    public static Sort createSort(final String sort) {
        String[] tokens   = sort.split(",");
        Sort     sortSpec = Sort.unsorted();

        for (int i = 0; i < tokens.length; i += 2) {
            String property  = tokens[i].trim();
            String direction = tokens[i + 1].trim().toUpperCase();

            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            sortSpec = sortSpec.and(Sort.by(sortDirection, property));
        }

        return sortSpec;
    }

}
