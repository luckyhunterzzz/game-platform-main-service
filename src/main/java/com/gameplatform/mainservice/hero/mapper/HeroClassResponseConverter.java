package com.gameplatform.mainservice.hero.mapper;

import com.gameplatform.mainservice.hero.domain.entity.HeroClass;
import com.gameplatform.mainservice.hero.dto.response.HeroClassResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HeroClassResponseConverter {

    public HeroClassResponse toResponse(HeroClass entity) {
        return new HeroClassResponse(
                entity.getId(),
                entity.getNameJson(),
                entity.getBaseNameJson(),
                entity.getBaseDescriptionJson(),
                entity.getMasterNameJson(),
                entity.getMasterDescriptionJson()
        );
    }

    public List<HeroClassResponse> toResponseList(List<HeroClass> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}