package com.gameplatform.mainservice.hero.converter;

import com.gameplatform.mainservice.hero.domain.entity.Element;
import com.gameplatform.mainservice.hero.dto.response.ElementResponse;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ElementResponseConverter {

    private final MediaUrlResolver mediaUrlResolver;

    public ElementResponse toResponse(Element entity) {
        return new ElementResponse(
                entity.getId(),
                entity.getNameJson(),
                entity.getImageBucket(),
                entity.getImageObjectKey(),
                mediaUrlResolver.resolveUrl(entity.getImageBucket(), entity.getImageObjectKey())
        );
    }

    public List<ElementResponse> toResponseList(List<Element> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
