package com.gameplatform.mainservice.hero.converter;

import com.gameplatform.mainservice.hero.domain.entity.HeroClass;
import com.gameplatform.mainservice.hero.dto.response.HeroClassResponse;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HeroClassResponseConverter {

    private final MediaUrlResolver mediaUrlResolver;

    public HeroClassResponse toResponse(HeroClass entity) {
        return new HeroClassResponse(
                entity.getId(),
                entity.getNameJson(),
                entity.getBaseNameJson(),
                entity.getBaseDescriptionJson(),
                entity.getMasterNameJson(),
                entity.getMasterDescriptionJson(),
                entity.getImageBucket(),
                entity.getImageObjectKey(),
                mediaUrlResolver.resolveUrl(entity.getImageBucket(), entity.getImageObjectKey())
        );
    }

    public List<HeroClassResponse> toResponseList(List<HeroClass> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
