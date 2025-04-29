package com.kyohwee.ojt.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

// 페이지 응답 구조
@Getter @AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int page;            // 0-based
    private int size;
    private long totalElements;
    private int totalPages;
}
