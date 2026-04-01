package com.gameplatform.mainservice.hero.converter;

import com.gameplatform.mainservice.hero.domain.entity.Hero;
import com.gameplatform.mainservice.hero.domain.entity.HeroPassiveSkill;
import com.gameplatform.mainservice.hero.dto.response.HeroResponse;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HeroResponseConverter {

    private final MediaUrlResolver mediaUrlResolver;

    public HeroResponse toResponse(Hero entity, List<HeroPassiveSkill> passiveSkills) {
        List<Long> passiveSkillIds = passiveSkills.stream()
                .map(link -> link.getId().getPassiveSkillId())
                .toList();

        return new HeroResponse(
                entity.getId(),
                entity.getSlug(),
                entity.getNameJson(),
                entity.getSpecialSkillNameJson(),
                entity.getSpecialSkillDescriptionJson(),
                entity.getBaseAttack(),
                entity.getBaseArmor(),
                entity.getBaseHp(),
                entity.getElementId(),
                entity.getRarityId(),
                entity.getHeroClassId(),
                entity.getFamilyId(),
                entity.getManaSpeedId(),
                entity.getAlphaTalentId(),
                entity.getImageBucket(),
                entity.getImageObjectKey(),
                mediaUrlResolver.resolveUrl(entity.getImageBucket(), entity.getImageObjectKey()),
                entity.isCostume(),
                entity.getBaseHeroId(),
                entity.getCostumeBonusJson(),
                entity.getReleaseDate(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy(),
                passiveSkillIds
        );
    }

    public List<HeroResponse> toResponseList(List<Hero> heroes, List<HeroPassiveSkill> allLinks) {
        return heroes.stream()
                .map(hero -> {
                    List<HeroPassiveSkill> heroLinks = allLinks.stream()
                            .filter(link -> link.getId().getHeroId().equals(hero.getId()))
                            .toList();

                    return toResponse(hero, heroLinks);
                })
                .toList();
    }
}
