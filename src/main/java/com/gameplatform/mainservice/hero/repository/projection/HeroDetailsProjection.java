package com.gameplatform.mainservice.hero.repository.projection;

import java.time.LocalDate;

public interface HeroDetailsProjection {
    Long getId();
    String getSlug();
    String getName();

    Long getElementId();
    String getElementName();

    Long getRarityId();
    Integer getRarityStars();

    Long getHeroClassId();
    String getHeroClassName();

    Long getFamilyId();
    String getFamilyName();

    Long getManaSpeedId();
    String getManaSpeedName();

    Long getAlphaTalentId();
    String getAlphaTalentName();

    String getSpecialSkillName();
    String getSpecialSkillDescription();

    String getImageBucket();
    String getImageObjectKey();
    String getPreviewBucket();
    String getPreviewObjectKey();

    Long getBaseHeroId();
    Boolean getIsCostume();
    LocalDate getReleaseDate();
}
