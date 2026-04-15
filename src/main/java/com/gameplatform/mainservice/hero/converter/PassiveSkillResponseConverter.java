package com.gameplatform.mainservice.hero.converter;

import com.gameplatform.mainservice.hero.domain.entity.PassiveSkill;
import com.gameplatform.mainservice.hero.dto.response.PassiveSkillResponse;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PassiveSkillResponseConverter {

    private final MediaUrlResolver mediaUrlResolver;

    public PassiveSkillResponse toResponse(PassiveSkill entity) {
        return new PassiveSkillResponse(
                entity.getId(),
                entity.getNameJson(),
                entity.getDescriptionJson(),
                entity.getImageBucket(),
                entity.getImageObjectKey(),
                mediaUrlResolver.resolveUrl(entity.getImageBucket(), entity.getImageObjectKey())
        );
    }

    public List<PassiveSkillResponse> toResponseList(List<PassiveSkill> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
