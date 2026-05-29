package com.gameplatform.mainservice.hero.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalHeroRecord(
        String name,
        @JsonProperty("fancy_name")
        String fancyName,
        String color,
        @JsonProperty("class")
        String heroClass,
        String skill,
        @JsonProperty("Release date")
        String releaseDate,
        String manaSpeedId,
        String aetherPowerId,
        String costumeBonus,
        String parentHeroId,
        List<String> passiveSkills,
        String heroId,
        Integer star,
        Integer power,
        Integer attack,
        Integer defense,
        Integer health,
        List<String> effects,
        List<String> passives,
        String family,
        @JsonProperty("costume_id")
        Integer costumeId
) {
}
