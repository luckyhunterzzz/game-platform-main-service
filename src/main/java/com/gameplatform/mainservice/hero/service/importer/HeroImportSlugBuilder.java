package com.gameplatform.mainservice.hero.service.importer;

import com.gameplatform.mainservice.hero.dto.external.ExternalHeroRecord;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class HeroImportSlugBuilder {

    public String buildSlug(ExternalHeroRecord hero) {
        if (hero == null) {
            return null;
        }

        String baseSlug = buildBaseSlug(hero.name());
        if (baseSlug == null) {
            return null;
        }

        if (trimToNull(hero.parentHeroId()) != null && !isCostume(hero)) {
            return null;
        }

        return isCostume(hero) ? baseSlug + "-c" + hero.costumeId() : baseSlug;
    }

    public String buildBaseSlug(String sourceName) {
        String normalizedName = stripTrailingCostumeMarkers(sourceName);
        if (normalizedName == null) {
            return null;
        }

        String normalized = Normalizer.normalize(normalizedName, Normalizer.Form.NFKD)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-+|-+$", "");

        return normalized.isBlank() ? null : normalized;
    }

    public boolean isCostume(ExternalHeroRecord hero) {
        return hero != null
                && hero.costumeId() != null
                && hero.costumeId() > 0;
    }

    private String stripTrailingCostumeMarkers(String sourceName) {
        String value = trimToNull(sourceName);
        if (value == null) {
            return null;
        }

        return value
                .replaceAll("(?i)(?:\\s+(?:c\\d+|toon|glass|stylish))+$", "")
                .trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
