package com.gameplatform.mainservice.hero.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record CatalogPageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static <T> CatalogPageResponse<T> from(Page<T> page) {
        return new CatalogPageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
