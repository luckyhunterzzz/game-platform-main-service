package com.gameplatform.mainservice.hero.repository.projection;

public interface HeroReferenceValidationProjection {

    Boolean getElementExists();

    Boolean getRarityExists();

    Boolean getHeroClassExists();

    Boolean getFamilyExists();

    Boolean getManaSpeedExists();

    Boolean getAlphaTalentExists();

    Boolean getBaseHeroExists();
}
