package com.gameplatform.mainservice.hero.converter;

import com.gameplatform.mainservice.hero.domain.entity.Rarity;
import com.gameplatform.mainservice.hero.dto.response.RarityResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RarityResponseConverter {

    public RarityResponse toResponse(Rarity entity) {
        return new RarityResponse(
                entity.getId(),
                entity.getNameJson(),
                entity.getStars()
        );
    }

    public List<RarityResponse> toResponseList(List<Rarity> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}