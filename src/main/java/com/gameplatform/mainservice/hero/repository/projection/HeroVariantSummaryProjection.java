package com.gameplatform.mainservice.hero.repository.projection;

public interface HeroVariantSummaryProjection {
    Long getId();
    String getSlug();
    String getName();
    Integer getCostumeIndex();
    String getImageBucket();
    String getImageObjectKey();
    String getElementName();
    String getRarityName();
    Integer getRarityStars();
}
