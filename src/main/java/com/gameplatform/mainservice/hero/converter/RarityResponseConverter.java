package com.gameplatform.mainservice.hero.converter;

import com.gameplatform.mainservice.hero.domain.entity.Rarity;
import com.gameplatform.mainservice.hero.dto.response.RarityResponse;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RarityResponseConverter {

    private final MediaUrlResolver mediaUrlResolver;

    public RarityResponse toResponse(Rarity entity) {
        return new RarityResponse(
                entity.getId(),
                entity.getNameJson(),
                entity.getStars(),
                entity.getImageBucket(),
                entity.getImageObjectKey(),
                mediaUrlResolver.resolveUrl(entity.getImageBucket(), entity.getImageObjectKey())
        );
    }

    public List<RarityResponse> toResponseList(List<Rarity> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
