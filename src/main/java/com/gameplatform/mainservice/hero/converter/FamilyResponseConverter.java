package com.gameplatform.mainservice.hero.converter;

import com.gameplatform.mainservice.hero.domain.entity.Family;
import com.gameplatform.mainservice.hero.dto.response.FamilyResponse;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FamilyResponseConverter {

    private final MediaUrlResolver mediaUrlResolver;

    public FamilyResponse toResponse(Family entity) {
        return new FamilyResponse(
                entity.getId(),
                entity.getNameJson(),
                entity.getDescriptionJson(),
                entity.getImageBucket(),
                entity.getImageObjectKey(),
                mediaUrlResolver.resolveUrl(entity.getImageBucket(), entity.getImageObjectKey())
        );
    }

    public List<FamilyResponse> toResponseList(List<Family> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
