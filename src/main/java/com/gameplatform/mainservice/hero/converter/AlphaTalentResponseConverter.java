package com.gameplatform.mainservice.hero.converter;

import com.gameplatform.mainservice.hero.domain.entity.AlphaTalent;
import com.gameplatform.mainservice.hero.dto.response.AlphaTalentResponse;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AlphaTalentResponseConverter {

    private final MediaUrlResolver mediaUrlResolver;

    public AlphaTalentResponse toResponse(AlphaTalent entity) {
        return new AlphaTalentResponse(
                entity.getId(),
                entity.getNameJson(),
                entity.getDescriptionJson(),
                entity.getImageBucket(),
                entity.getImageObjectKey(),
                mediaUrlResolver.resolveUrl(entity.getImageBucket(), entity.getImageObjectKey())
        );
    }

    public List<AlphaTalentResponse> toResponseList(List<AlphaTalent> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
