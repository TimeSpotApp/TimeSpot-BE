package com.timespot.backend.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * PackageName : com.timespot.backend.common.util
 * FileName    : SortUtilsTest
 * Author      : loadingKKamo21
 * Date        : 26. 3. 26.
 * Description : 정렬 유틸리티 클래스 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 26.    loadingKKamo21       Initial creation
 */
class SortUtilsTest {

    @Test
    @DisplayName("단일 정렬 문자열로 Sort 객체를 생성한다")
    void createSort_withSingleSort() {
        // given
        String sort = "createdAt,DESC";

        // when
        Sort result = SortUtils.createSort(sort);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderFor("createdAt")).isEqualTo(Sort.Order.desc("createdAt"));
    }

    @Test
    @DisplayName("다중 정렬 문자열로 Sort 객체를 생성한다")
    void createSort_withMultipleSorts() {
        // given
        String sort = "createdAt,DESC,stationName,ASC,visitCount,DESC";

        // when
        Sort result = SortUtils.createSort(sort);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderFor("createdAt")).isEqualTo(Sort.Order.desc("createdAt"));
        assertThat(result.getOrderFor("stationName")).isEqualTo(Sort.Order.asc("stationName"));
        assertThat(result.getOrderFor("visitCount")).isEqualTo(Sort.Order.desc("visitCount"));
    }

    @Test
    @DisplayName("소문자 방향도 대문자로 변환되어 처리된다")
    void createSort_withLowerCaseDirection() {
        // given
        String sort = "createdAt,desc";

        // when
        Sort result = SortUtils.createSort(sort);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderFor("createdAt")).isEqualTo(Sort.Order.desc("createdAt"));
    }

    @Test
    @DisplayName("공백이 포함된 정렬 문자열도 정상 처리된다")
    void createSort_withWhitespace() {
        // given
        String sort = "createdAt , DESC , stationName , ASC";

        // when
        Sort result = SortUtils.createSort(sort);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderFor("createdAt")).isEqualTo(Sort.Order.desc("createdAt"));
        assertThat(result.getOrderFor("stationName")).isEqualTo(Sort.Order.asc("stationName"));
    }

    @Test
    @DisplayName("createPageable 은 0 기반 페이지로 변환한다")
    void createPageable_convertsToOneBasedPage() {
        // given
        int    page = 1;
        int    size = 10;
        String sort = "createdAt,DESC";

        // when
        Pageable result = SortUtils.createPageable(page, size, sort);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getSort().getOrderFor("createdAt")).isEqualTo(Sort.Order.desc("createdAt"));
    }

    @Test
    @DisplayName("여러 페이지에서도 올바르게 0 기반 인덱스로 변환한다")
    void createPageable_withDifferentPages() {
        // given & when
        Pageable page1 = SortUtils.createPageable(1, 10, "createdAt,DESC");
        Pageable page2 = SortUtils.createPageable(2, 10, "createdAt,DESC");
        Pageable page3 = SortUtils.createPageable(3, 10, "createdAt,DESC");

        // then
        assertThat(page1.getPageNumber()).isEqualTo(0);
        assertThat(page2.getPageNumber()).isEqualTo(1);
        assertThat(page3.getPageNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("정렬 방향이 ASC 일 때 올바르게 처리된다")
    void createSort_withAscDirection() {
        // given
        String sort = "name,ASC";

        // when
        Sort result = SortUtils.createSort(sort);

        // then
        assertThat(result.getOrderFor("name")).isEqualTo(Sort.Order.asc("name"));
    }

}
