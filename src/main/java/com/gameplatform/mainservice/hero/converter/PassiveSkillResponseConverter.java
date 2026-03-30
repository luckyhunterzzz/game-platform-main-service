package com.gameplatform.mainservice.hero.converter;

import com.gameplatform.mainservice.hero.domain.entity.PassiveSkill;
import com.gameplatform.mainservice.hero.dto.response.PassiveSkillResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PassiveSkillResponseConverter {

    public PassiveSkillResponse toResponse(PassiveSkill entity) {
        return new PassiveSkillResponse(
                entity.getId(),
                entity.getNameJson(),
                entity.getDescriptionJson()
        );
    }

    public List<PassiveSkillResponse> toResponseList(List<PassiveSkill> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}