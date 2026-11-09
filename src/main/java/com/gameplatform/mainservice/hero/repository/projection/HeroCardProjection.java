package com.gameplatform.mainservice.hero.repository.projection;

import java.time.LocalDate;

public interface HeroCardProjection {
    Long getId();
    String getSlug();
    String getName();
    Long getBaseHeroId();
    Boolean getIsCostume();
    Integer getCostumeIndex();
    String getImageBucket();
    String getImageObjectKey();
    String getPreviewBucket();
    String getPreviewObjectKey();
    String getElementName();
    String getElementImageBucket();
    String getElementImageObjectKey();
    String getRarityName();
    Integer getRarityStars();
    String getHeroClassName();
    String getHeroClassImageBucket();
    String getHeroClassImageObjectKey();
    String getManaSpeedName();
    String getFamilyName();
    String getAlphaTalentName();
    Integer getBaseAttack();
    Integer getBaseArmor();
    Integer getBaseHp();
    LocalDate getReleaseDate();
}
