package com.gameplatform.mainservice.hero.repository.projection;

import java.time.LocalDate;

public interface HeroCoachHeroProjection {
    Long getId();
    String getSlug();
    String getName();
    String getImageBucket();
    String getImageObjectKey();
    String getPreviewBucket();
    String getPreviewObjectKey();
    String getElementName();
    String getRarityName();
    Integer getRarityStars();
    String getHeroClassName();
    String getManaSpeedName();
    String getFamilyName();
    String getAlphaTalentName();
    Integer getBaseAttack();
    Integer getBaseArmor();
    Integer getBaseHp();
    LocalDate getReleaseDate();
    LocalDate getHeroCoachDate();
}
