package com.gameplatform.mainservice.hero.repository.projection;

public record HeroCardRow(
        Long id,
        String slug,
        String name,
        Long baseHeroId,
        Boolean isCostume,
        Integer costumeIndex,
        String imageBucket,
        String imageObjectKey,
        String previewBucket,
        String previewObjectKey,
        String elementName,
        String rarityName,
        Integer rarityStars,
        String heroClassName,
        String manaSpeedName,
        String familyName,
        String alphaTalentName,
        Integer baseAttack,
        Integer baseArmor,
        Integer baseHp
) implements HeroCardProjection {

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getSlug() {
        return slug;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Long getBaseHeroId() {
        return baseHeroId;
    }

    @Override
    public Boolean getIsCostume() {
        return isCostume;
    }

    @Override
    public Integer getCostumeIndex() {
        return costumeIndex;
    }

    @Override
    public String getImageBucket() {
        return imageBucket;
    }

    @Override
    public String getImageObjectKey() {
        return imageObjectKey;
    }

    @Override
    public String getPreviewBucket() {
        return previewBucket;
    }

    @Override
    public String getPreviewObjectKey() {
        return previewObjectKey;
    }

    @Override
    public String getElementName() {
        return elementName;
    }

    @Override
    public String getRarityName() {
        return rarityName;
    }

    @Override
    public Integer getRarityStars() {
        return rarityStars;
    }

    @Override
    public String getHeroClassName() {
        return heroClassName;
    }

    @Override
    public String getManaSpeedName() {
        return manaSpeedName;
    }

    @Override
    public String getFamilyName() {
        return familyName;
    }

    @Override
    public String getAlphaTalentName() {
        return alphaTalentName;
    }

    @Override
    public Integer getBaseAttack() {
        return baseAttack;
    }

    @Override
    public Integer getBaseArmor() {
        return baseArmor;
    }

    @Override
    public Integer getBaseHp() {
        return baseHp;
    }
}
