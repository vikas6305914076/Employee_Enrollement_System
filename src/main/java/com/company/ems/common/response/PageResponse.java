package com.company.ems.common.response;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        String sortBy,
        String direction,
        boolean first,
        boolean last
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        Sort.Order order = page.getSort().stream().findFirst().orElse(Sort.Order.asc("createdAt"));
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                order.getProperty(),
                order.getDirection().name(),
                page.isFirst(),
                page.isLast()
        );
    }
}
