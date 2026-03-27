package com.timespot.backend.common.response.util;

import com.timespot.backend.common.response.annotation.CustomPageResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * PackageName : com.timespot.backend.common.response.util
 * FileName    : PageResponseConverter
 * Author      : loadingKKamo21
 * Date        : 26. 3. 5.
 * Description : Page 객체를 커스텀 응답 형식으로 변환하는 유틸리티
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 5.     loadingKKamo21       Initial creation
 * 26. 3. 26.    loadingKKamo21       재귀적 Page 변환 로직 추가
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PageResponseConverter {

    public static Map<String, Object> convertPageToCustomMap(final Page<?> page, final CustomPageResponse annotation) {
        Map<String, Object> map = new HashMap<>();
        if (annotation.content()) map.put("content", page.getContent());
        if (annotation.totalElements()) map.put("totalElements", page.getTotalElements());
        if (annotation.totalPages()) map.put("totalPages", page.getTotalPages());
        if (annotation.size()) map.put("size", page.getSize());
        if (annotation.number()) map.put("number", page.getNumber());
        if (annotation.numberOfElements()) map.put("numberOfElements", page.getNumberOfElements());
        if (annotation.sort()) map.put("sort", page.getSort());
        if (annotation.empty()) map.put("empty", page.isEmpty());
        if (annotation.hasContent()) map.put("hasContent", page.hasContent());
        if (annotation.first()) map.put("first", page.isFirst());
        if (annotation.last()) map.put("last", page.isLast());
        if (annotation.hasPrevious()) map.put("hasPrevious", page.hasPrevious());
        if (annotation.hasNext()) map.put("hasNext", page.hasNext());
        return map;
    }

    /**
     * 객체 내부의 모든 Page 를 재귀적으로 탐색하여 Map 으로 변환
     *
     * @param obj        변환할 객체
     * @param annotation CustomPageResponse 어노테이션
     * @return 변환된 객체 (Page 는 Map 으로 변환됨)
     */
    @SuppressWarnings("unchecked")
    public static Object convertNestedPageToObject(final Object obj, final CustomPageResponse annotation) {
        if (obj == null) return null;

        if (obj instanceof Page) return convertPageToCustomMap((Page<?>) obj, annotation);

        if (obj instanceof List)
            return ((List<?>) obj).stream()
                                  .map(element -> convertNestedPageToObject(element, annotation))
                                  .toList();

        if (obj instanceof Map) {
            Map<?, ?>           originalMap = (Map<?, ?>) obj;
            Map<Object, Object> resultMap   = new HashMap<>();
            originalMap.forEach((key, value) -> resultMap.put(key, convertNestedPageToObject(value, annotation)));
            return resultMap;
        }

        if (obj.getClass().getName().startsWith("java.") || obj.getClass().isEnum()) return obj;

        return convertObjectFields(obj, annotation);
    }

    /**
     * 객체의 필드를 재귀적으로 탐색하여 Page 변환
     *
     * @param obj        변환할 객체
     * @param annotation CustomPageResponse 어노테이션
     * @return 변환된 객체
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> convertObjectFields(final Object obj, final CustomPageResponse annotation) {
        Map<String, Object> resultMap = new HashMap<>();

        for (var method : obj.getClass().getDeclaredMethods())
            if (method.getName().startsWith("get")
                && method.getParameterCount() == 0
                && !method.getReturnType().equals(Void.TYPE))
                try {
                    if (method.getName().equals("getClass")) continue;

                    Object value = method.invoke(obj);
                    resultMap.put(toCamelCase(method.getName()), convertNestedPageToObject(value, annotation));
                } catch (Exception e) {
                    // 무시
                }

        return resultMap;
    }

    /**
     * 메서드 이름을 카멜케이스로 변환
     * 예: "getStations" → "stations"
     */
    private static String toCamelCase(final String methodName) {
        if (methodName.startsWith("get")) {
            String fieldName = methodName.substring(3);
            return Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
        }
        return methodName;
    }

}
