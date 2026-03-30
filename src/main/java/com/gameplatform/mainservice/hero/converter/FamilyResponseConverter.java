package com.gameplatform.mainservice.hero.converter;

import com.gameplatform.mainservice.hero.domain.entity.Family;
import com.gameplatform.mainservice.hero.dto.response.FamilyResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FamilyResponseConverter {

    public FamilyResponse toResponse(Family entity) {
        return new FamilyResponse(
                entity.getId(),
                entity.getNameJson(),
                entity.getDescriptionJson()
        );
    }

    public List<FamilyResponse> toResponseList(List<Family> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}