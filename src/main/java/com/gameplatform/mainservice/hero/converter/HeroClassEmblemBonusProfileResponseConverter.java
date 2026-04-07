package com.gameplatform.mainservice.hero.converter;

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
                entity.getAttackFlatBonus(),
                entity.getArmorFlatBonus(),
                entity.getHpFlatBonus(),
                entity.getAttackPercentBonus(),
                entity.getArmorPercentBonus(),
                entity.getHpPercentBonus(),
                entity.getMasterAttackBonus(),
                entity.getMasterArmorBonus(),
                entity.getMasterHpBonus()
        );
    }

    public List<HeroClassEmblemBonusProfileResponse> toResponseList(List<HeroClassEmblemBonusProfile> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
