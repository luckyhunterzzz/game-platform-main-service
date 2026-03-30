package com.gameplatform.mainservice.hero.converter;

import com.gameplatform.mainservice.hero.domain.entity.ManaSpeed;
import com.gameplatform.mainservice.hero.dto.response.ManaSpeedResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ManaSpeedResponseConverter {

    public ManaSpeedResponse toResponse(ManaSpeed entity) {
        return new ManaSpeedResponse(
                entity.getId(),
                entity.getNameJson(),
                entity.getDescriptionJson()
        );
    }

    public List<ManaSpeedResponse> toResponseList(List<ManaSpeed> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}