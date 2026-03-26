package com.gameplatform.mainservice.hero.mapper;

import com.gameplatform.mainservice.hero.domain.entity.HeroClassEmblemBonusProfile;
import com.gameplatform.mainservice.hero.dto.response.HeroClassEmblemBonusProfileResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HeroClassEmblemBonusProfileResponseConverter {

    public HeroClassEmblemBonusProfileResponse toResponse(HeroClassEmblemBonusProfile entity) {
        return new HeroClassEmblemBonusProfileResponse(
                entity.getId(),
                entity.getHeroClassId(),
                entity.getPathType(),
                entity.getAttackBonus(),
                entity.getArmorBonus(),
                entity.getHpBonus()
        );
    }

    public List<HeroClassEmblemBonusProfileResponse> toResponseList(List<HeroClassEmblemBonusProfile> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}