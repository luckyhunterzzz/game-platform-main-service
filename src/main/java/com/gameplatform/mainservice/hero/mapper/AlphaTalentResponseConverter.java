package com.gameplatform.mainservice.hero.mapper;

import com.gameplatform.mainservice.hero.domain.entity.AlphaTalent;
import com.gameplatform.mainservice.hero.dto.response.AlphaTalentResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlphaTalentResponseConverter {

    public AlphaTalentResponse toResponse(AlphaTalent entity) {
        return new AlphaTalentResponse(
                entity.getId(),
                entity.getNameJson(),
                entity.getDescriptionJson()
        );
    }

    public List<AlphaTalentResponse> toResponseList(List<AlphaTalent> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}