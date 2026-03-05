package com.nomadspot.backend.common.response.util;

import com.nomadspot.backend.common.response.annotation.CustomPageResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * PackageName : com.nomadspot.backend.common.response.util
 * FileName    : PageResponseConverter
 * Author      : loadingKKamo21
 * Date        : 26. 3. 5.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 5.     loadingKKamo21       Initial creation
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

}
