package com.gameplatform.mainservice.hero.dto.response;

public record DescribedReferenceResponse(
        Long id,
        String name,
        String description,
        String imageUrl
) {
}
