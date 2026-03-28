package com.timespot.backend.common.response.dto;

import lombok.Getter;
import org.springframework.data.domain.Slice;
import java.util.List;

public class SliceResponseDto<T> {
    @Getter
    private final List<T> content;

    private final boolean hasNext;

    public SliceResponseDto(Slice<T> slice) {
        this.content = slice.getContent();
        this.hasNext = slice.hasNext();
    }

    public boolean getHasNext() {
        return hasNext;
    }
}