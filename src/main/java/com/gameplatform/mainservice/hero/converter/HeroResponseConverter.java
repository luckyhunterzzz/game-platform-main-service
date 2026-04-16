package com.gameplatform.mainservice.hero.converter;

import com.gameplatform.mainservice.hero.domain.entity.Hero;
import com.gameplatform.mainservice.hero.domain.entity.HeroPassiveSkill;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
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
                entity.getImageBucketJson(),
                entity.getImageObjectKeyJson(),
                resolveImageUrls(entity.getImageBucketJson(), entity.getImageObjectKeyJson()),
                entity.getPreviewBucket(),
                entity.getPreviewObjectKey(),
                mediaUrlResolver.resolveUrl(entity.getPreviewBucket(), entity.getPreviewObjectKey()),
                entity.isCostume(),
                entity.getBaseHeroId(),
                entity.getCostumeIndex(),
                entity.getCostumeBonusJson(),
                entity.getReleaseDate(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedByEmail(),
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

    private LocalizedTextJson resolveImageUrls(LocalizedTextJson imageBucketJson, LocalizedTextJson imageObjectKeyJson) {
        if (imageBucketJson == null || imageObjectKeyJson == null) {
            return null;
        }

        String ru = mediaUrlResolver.resolveUrl(imageBucketJson.ru(), imageObjectKeyJson.ru());
        String en = mediaUrlResolver.resolveUrl(imageBucketJson.en(), imageObjectKeyJson.en());

        if (ru == null && en == null) {
            return null;
        }

        return new LocalizedTextJson(ru, en);
    }
}
