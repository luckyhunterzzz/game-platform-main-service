package com.gameplatform.mainservice.hero.mapper;

import com.gameplatform.mainservice.hero.domain.entity.RarityEvolutionMultiplier;
import com.gameplatform.mainservice.hero.dto.response.RarityEvolutionMultiplierResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RarityEvolutionMultiplierResponseConverter {

    public RarityEvolutionMultiplierResponse toResponse(RarityEvolutionMultiplier entity) {
        return new RarityEvolutionMultiplierResponse(
                entity.getId(),
                entity.getRarityId(),
                entity.getStageCode(),
                entity.getAttackMultiplier(),
                entity.getArmorMultiplier(),
                entity.getHpMultiplier()
        );
    }

    public List<RarityEvolutionMultiplierResponse> toResponseList(List<RarityEvolutionMultiplier> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}