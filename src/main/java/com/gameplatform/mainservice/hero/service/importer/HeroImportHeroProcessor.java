package com.gameplatform.mainservice.hero.service.importer;

import com.gameplatform.mainservice.hero.domain.entity.Hero;
import com.gameplatform.mainservice.hero.domain.enums.HeroStatus;
import com.gameplatform.mainservice.hero.dto.external.ExternalHeroRecord;
import com.gameplatform.mainservice.hero.dto.external.ExternalLocalizedHeroRecord;
import com.gameplatform.mainservice.hero.dto.json.CostumeBonusJson;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.hero.dto.request.HeroCatalogImportRequest;
import com.gameplatform.mainservice.hero.dto.request.HeroImportParentMode;
import com.gameplatform.mainservice.hero.dto.request.HeroUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroCatalogImportPlannedHeroResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroCatalogImportSkippedItemResponse;
import com.gameplatform.mainservice.hero.service.HeroAdminService;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import com.gameplatform.mainservice.media.model.StoredImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class HeroImportHeroProcessor {

    public static final LocalDate DEFAULT_UNKNOWN_RELEASE_DATE = LocalDate.of(2017, 6, 1);
    public static final String FULL_IMAGE_URL_TEMPLATE =
            "https://cdn.jsdelivr.net/gh/vabe44/31f5d518-epzl-cdn@main/hero_cards/full/max/%s.webp";
    public static final String PREVIEW_IMAGE_URL_TEMPLATE =
            "https://cdn.jsdelivr.net/gh/vabe44/31f5d518-epzl-cdn@main/portraits/%s.webp";

    private final HeroRepository heroRepository;
    private final HeroAdminService heroAdminService;
    private final HeroImportSlugBuilder slugBuilder;
    private final HeroImportElementIdResolver elementIdResolver;
    private final HeroImportHeroClassIdResolver heroClassIdResolver;
    private final HeroImportManaSpeedIdResolver manaSpeedIdResolver;
    private final HeroImportRarityIdResolver rarityIdResolver;
    private final HeroImportAlphaTalentIdResolver alphaTalentIdResolver;
    private final HeroImportImageService imageService;
    private final HeroImportSourceValueNormalizer normalizer;

    public HeroImportHeroProcessingResult process(
            ExternalHeroRecord hero,
            ExternalLocalizedHeroRecord localizedHero,
            HeroCatalogImportRequest request,
            boolean dryRun,
            HeroImportDictionaryLookup lookup
    ) {
        String slug = slugBuilder.buildSlug(hero);
        String heroId = trimToNull(hero.heroId());
        String heroName = trimToNull(hero.name());

        if (slug == null || heroId == null || heroName == null) {
            return HeroImportHeroProcessingResult.skippedUnresolved(
                    new HeroCatalogImportSkippedItemResponse(heroId, heroName, slug, "Missing required hero identity fields")
            );
        }

        if (heroRepository.existsBySlug(slug)) {
            return HeroImportHeroProcessingResult.skippedExisting(
                    new HeroCatalogImportSkippedItemResponse(heroId, heroName, slug, "Hero already exists")
            );
        }

        Resolution resolution = resolveReferences(hero, lookup, request.parentMode());
        if (!resolution.isResolved()) {
            return HeroImportHeroProcessingResult.skippedUnresolved(
                    new HeroCatalogImportSkippedItemResponse(heroId, heroName, slug, resolution.reason())
            );
        }

        try {
            String fullImageEnSourceUrl = FULL_IMAGE_URL_TEMPLATE.formatted(heroId);
            String previewImageSourceUrl = PREVIEW_IMAGE_URL_TEMPLATE.formatted(heroId);
            String fullImageRuSourceUrl = resolveLocalizedImageSourceUrl(localizedHero, fullImageEnSourceUrl);
            StoredImage fullImageEn = dryRun ? null : imageService.tryDownloadAndStoreImage(heroId, fullImageEnSourceUrl, "full-en");
            StoredImage fullImageRu = dryRun ? null : imageService.tryDownloadAndStoreImage(heroId, fullImageRuSourceUrl, "full-ru");
            StoredImage previewImage = dryRun ? null : imageService.tryDownloadAndStoreImage(heroId, previewImageSourceUrl, "preview");
            StoredImage effectiveRuImage = fullImageRu != null ? fullImageRu : fullImageEn;

            HeroUpsertRequest upsertRequest = buildUpsertRequest(
                    request,
                    hero,
                    localizedHero,
                    slug,
                    resolution,
                    fullImageEn,
                    effectiveRuImage,
                    previewImage
            );
            HeroCatalogImportPlannedHeroResponse plannedHero = buildPlannedHeroResponse(
                    hero,
                    slug,
                    resolution,
                    upsertRequest.status(),
                    fullImageEnSourceUrl,
                    fullImageRuSourceUrl,
                    previewImageSourceUrl
            );

            if (dryRun) {
                log.info("DRY RUN hero import preview. hero={}, payload={}", plannedHero, upsertRequest);
            } else {
                heroAdminService.create(upsertRequest);
            }

            return HeroImportHeroProcessingResult.created(slug, plannedHero);
        } catch (Exception e) {
            return HeroImportHeroProcessingResult.skippedUnresolved(
                    new HeroCatalogImportSkippedItemResponse(heroId, heroName, slug, rootCauseMessage(e))
            );
        }
    }

    public boolean matchesReleaseDateRange(ExternalHeroRecord hero, HeroCatalogImportRequest request) {
        if (request.releaseDateFrom() == null && request.releaseDateTo() == null) {
            return true;
        }

        LocalDate releaseDate = resolveReleaseDate(hero.releaseDate());
        if (request.releaseDateFrom() != null && releaseDate.isBefore(request.releaseDateFrom())) {
            return false;
        }

        return request.releaseDateTo() == null || !releaseDate.isAfter(request.releaseDateTo());
    }

    public boolean matchesParentMode(ExternalHeroRecord hero, HeroImportParentMode parentMode) {
        return switch (parentMode) {
            case ROOT_ONLY -> trimToNull(hero.parentHeroId()) == null;
            case COSTUMES_ONLY -> trimToNull(hero.parentHeroId()) != null;
        };
    }

    public String buildBaseSlug(String sourceName) {
        return slugBuilder.buildBaseSlug(sourceName);
    }

    private HeroUpsertRequest buildUpsertRequest(
            HeroCatalogImportRequest request,
            ExternalHeroRecord hero,
            ExternalLocalizedHeroRecord localizedHero,
            String slug,
            Resolution resolution,
            StoredImage fullImageEn,
            StoredImage fullImageRu,
            StoredImage previewImage
    ) {
        String englishName = firstNonBlank(
                normalizeLocalizedHeroName(hero.name(), hero),
                trimToNull(hero.name())
        );
        String russianName = firstNonBlank(
                normalizeLocalizedHeroName(localizedHero != null ? localizedHero.name() : null, hero),
                englishName
        );
        ParsedLocalizedDescription parsedLocalizedDescription = parseLocalizedDescription(localizedHero != null ? localizedHero.description() : null);
        String englishSkillName = trimToNull(hero.skill());
        String englishSkillDescription = joinLines(hero.effects());
        String russianSkillName = firstNonBlank(parsedLocalizedDescription.skillName(), englishSkillName);
        String russianSkillDescription = firstNonBlank(parsedLocalizedDescription.skillDescription(), englishSkillDescription);
        LocalizedTextJson localizedName = new LocalizedTextJson(russianName, englishName);
        LocalizedTextJson localizedSkillName = new LocalizedTextJson(russianSkillName, englishSkillName);
        LocalizedTextJson localizedSkillDescription = new LocalizedTextJson(russianSkillDescription, englishSkillDescription);
        LocalizedTextJson imageBucketJson = fullImageEn == null
                ? null
                : new LocalizedTextJson(
                        fullImageRu != null ? fullImageRu.bucket() : fullImageEn.bucket(),
                        fullImageEn.bucket()
                );
        LocalizedTextJson imageObjectKeyJson = fullImageEn == null
                ? null
                : new LocalizedTextJson(
                        fullImageRu != null ? fullImageRu.objectKey() : fullImageEn.objectKey(),
                        fullImageEn.objectKey()
                );

        return new HeroUpsertRequest(
                slug,
                localizedName,
                localizedSkillName,
                localizedSkillDescription,
                hero.attack(),
                hero.defense(),
                hero.health(),
                hero.power(),
                resolution.elementId(),
                resolution.rarityId(),
                resolution.heroClassId(),
                resolution.familyId(),
                resolution.manaSpeedId(),
                resolution.alphaTalentId(),
                imageBucketJson,
                imageObjectKeyJson,
                previewImage != null ? previewImage.bucket() : null,
                previewImage != null ? previewImage.objectKey() : null,
                resolution.costume(),
                resolution.baseHeroId(),
                resolution.costumeIndex(),
                resolution.costumeBonusJson(),
                resolveReleaseDate(hero.releaseDate()),
                 HeroStatus.DRAFT,
                 request.updatedBy().trim(),
                 trimToNull(request.updatedByEmail()),
                 null,
                 List.of(),
                 List.of()
         );
    }

    private HeroCatalogImportPlannedHeroResponse buildPlannedHeroResponse(
            ExternalHeroRecord hero,
            String slug,
            Resolution resolution,
            HeroStatus status,
            String fullImageEnSourceUrl,
            String fullImageRuSourceUrl,
            String previewImageSourceUrl
    ) {
        String baseHeroSlug = resolution.costume() ? slugBuilder.buildBaseSlug(hero.name()) : null;
        return new HeroCatalogImportPlannedHeroResponse(
                trimToNull(hero.heroId()),
                trimToNull(hero.name()),
                slug,
                resolution.costume(),
                baseHeroSlug,
                resolveReleaseDate(hero.releaseDate()).toString(),
                status.name(),
                fullImageEnSourceUrl,
                fullImageRuSourceUrl,
                previewImageSourceUrl
        );
    }

    private Resolution resolveReferences(
            ExternalHeroRecord hero,
            HeroImportDictionaryLookup lookup,
            HeroImportParentMode parentMode
    ) {
        Long rarityId = rarityIdResolver.resolveId(hero.star());
        if (rarityId == null) {
            return Resolution.unresolved("Rarity mapping not found for star " + hero.star());
        }

        Long elementId = elementIdResolver.resolveId(hero.color());
        if (elementId == null) {
            return Resolution.unresolved("Element mapping not found for color " + hero.color());
        }

        Long heroClassId = heroClassIdResolver.resolveId(hero.heroClass());
        if (heroClassId == null) {
            return Resolution.unresolved("Hero class mapping not found for class " + hero.heroClass());
        }

        Long manaSpeedId = manaSpeedIdResolver.resolveId(hero.manaSpeedId());
        if (manaSpeedId == null) {
            return Resolution.unresolved("Mana speed mapping not found for manaSpeedId " + hero.manaSpeedId());
        }

        Long alphaTalentId = firstNonNull(
                alphaTalentIdResolver.resolveId(hero.aetherPowerId()),
                resolveByAlias(hero.aetherPowerId(), lookup.alphaTalentIdByAlias(), this::humanizeIdentifier)
        );
        if (alphaTalentId == null) {
            return Resolution.unresolved("Alpha talent mapping not found for aetherPowerId " + hero.aetherPowerId());
        }

        Long familyId = resolveFamilyId(hero.family(), lookup);

        if (parentMode == HeroImportParentMode.ROOT_ONLY) {
            return Resolution.root(elementId, rarityId, heroClassId, manaSpeedId, familyId, alphaTalentId);
        }

        if (!slugBuilder.isCostume(hero)) {
            return Resolution.unresolved("Costume record requires positive costume_id");
        }

        String parentSlug = slugBuilder.buildBaseSlug(hero.name());
        if (parentSlug == null) {
            return Resolution.unresolved("Parent hero slug could not be generated");
        }

        Optional<Hero> parentDbHero = heroRepository.findBySlug(parentSlug);
        if (parentDbHero.isEmpty()) {
            return Resolution.unresolved("Parent hero not found in database by slug " + parentSlug);
        }

        CostumeBonusJson costumeBonusJson = new CostumeBonusJson(0, 0, 0, 0);
        return Resolution.costume(
                elementId,
                rarityId,
                heroClassId,
                manaSpeedId,
                familyId,
                alphaTalentId,
                parentDbHero.get().getId(),
                hero.costumeId(),
                costumeBonusJson
        );
    }

    private Long resolveFamilyId(String family, HeroImportDictionaryLookup lookup) {
        if (trimToNull(family) == null) {
            return null;
        }
        return resolveByAlias(family, lookup.familyIdByAlias(), this::humanizeIdentifier);
    }

    private Long resolveByAlias(String sourceValue, Map<String, Long> aliases, java.util.function.Function<String, String> fallbackTransformer) {
        String normalizedValue = normalizer.normalize(sourceValue);
        if (normalizedValue == null) {
            return null;
        }

        Long directMatch = aliases.get(normalizedValue);
        if (directMatch != null) {
            return directMatch;
        }

        String transformedValue = normalizer.normalize(fallbackTransformer.apply(sourceValue));
        return transformedValue == null ? null : aliases.get(transformedValue);
    }

    private String humanizeIdentifier(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }

        String[] parts = normalized.replace('-', '_').split("_");
        List<String> words = new java.util.ArrayList<>();
        for (String part : parts) {
            String cleanPart = trimToNull(part);
            if (cleanPart == null) {
                continue;
            }
            words.add(cleanPart.substring(0, 1).toUpperCase(Locale.ROOT) + cleanPart.substring(1).toLowerCase(Locale.ROOT));
        }
        return words.isEmpty() ? null : String.join(" ", words);
    }

    private LocalizedTextJson placeholder(String value) {
        String normalized = trimToNull(value);
        return new LocalizedTextJson(normalized, normalized);
    }

    private ParsedLocalizedDescription parseLocalizedDescription(String rawDescription) {
        String normalized = trimToNull(rawDescription);
        if (normalized == null) {
            return new ParsedLocalizedDescription(null, null);
        }

        String[] lines = normalized.split("\\R", -1);
        String firstLine = null;
        List<String> remainingLines = new ArrayList<>();
        for (String line : lines) {
            String cleanedLine = trimToNull(line);
            if (cleanedLine == null) {
                continue;
            }
            if (firstLine == null) {
                firstLine = cleanedLine;
            } else {
                remainingLines.add(cleanedLine);
            }
        }

        if (firstLine == null) {
            return new ParsedLocalizedDescription(null, null);
        }

        if (!looksLikeLocalizedSkillTitle(firstLine)) {
            return new ParsedLocalizedDescription(null, normalized);
        }

        String skillName = sentenceCase(firstLine);
        String skillDescription = remainingLines.isEmpty() ? null : String.join("\n", remainingLines);
        return new ParsedLocalizedDescription(skillName, skillDescription);
    }

    private String resolveLocalizedImageSourceUrl(ExternalLocalizedHeroRecord localizedHero, String fallbackUrl) {
        if (localizedHero == null || localizedHero.imageUrls() == null || localizedHero.imageUrls().isEmpty()) {
            return fallbackUrl;
        }
        return firstNonBlank(localizedHero.imageUrls().get(0), fallbackUrl);
    }

    private String normalizeLocalizedHeroName(String localizedName, ExternalHeroRecord hero) {
        String normalized = trimToNull(localizedName);
        if (normalized == null) {
            return null;
        }

        if (!slugBuilder.isCostume(hero)) {
            return normalized;
        }

        return normalized
                .replaceAll("(?iu)(?:\\s+к(?:\\d+)?)$", "")
                .replaceAll("(?iu)(?:\\s+[cсkк]\\d*)$", "")
                .replaceAll("(?iu)(?:\\s+[cсkк]\\d*)$", "")
                .trim();
    }

    private boolean looksLikeLocalizedSkillTitle(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return false;
        }

        boolean hasLetter = false;
        for (int index = 0; index < normalized.length(); index++) {
            char current = normalized.charAt(index);
            if (!Character.isLetter(current)) {
                continue;
            }

            hasLetter = true;
            if (Character.isLowerCase(current)) {
                return false;
            }
        }

        return hasLetter;
    }

    private LocalDate parseReleaseDate(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }
        return LocalDate.parse(normalized);
    }

    private LocalDate resolveReleaseDate(String value) {
        LocalDate parsed = parseReleaseDate(value);
        return parsed != null ? parsed : DEFAULT_UNKNOWN_RELEASE_DATE;
    }

    private String joinLines(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return null;
        }
        return lines.stream()
                .map(this::trimToNull)
                .filter(Objects::nonNull)
                .reduce((left, right) -> left + "\n" + right)
                .orElse(null);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String sentenceCase(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }

        String lower = normalized.toLowerCase(Locale.ROOT);
        return lower.substring(0, 1).toUpperCase(Locale.ROOT) + lower.substring(1);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String normalized = trimToNull(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    @SafeVarargs
    private <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = trimToNull(current.getMessage());
        return message != null ? message : current.getClass().getSimpleName();
    }

    private record Resolution(
            boolean costume,
            Long elementId,
            Long rarityId,
            Long heroClassId,
            Long manaSpeedId,
            Long familyId,
            Long alphaTalentId,
            Long baseHeroId,
            Integer costumeIndex,
            CostumeBonusJson costumeBonusJson,
            String reason
    ) {
        private static Resolution root(
                Long elementId,
                Long rarityId,
                Long heroClassId,
                Long manaSpeedId,
                Long familyId,
                Long alphaTalentId
        ) {
            return new Resolution(false, elementId, rarityId, heroClassId, manaSpeedId, familyId, alphaTalentId, null, null, null, null);
        }

        private static Resolution costume(
                Long elementId,
                Long rarityId,
                Long heroClassId,
                Long manaSpeedId,
                Long familyId,
                Long alphaTalentId,
                Long baseHeroId,
                Integer costumeIndex,
                CostumeBonusJson costumeBonusJson
        ) {
            return new Resolution(true, elementId, rarityId, heroClassId, manaSpeedId, familyId, alphaTalentId, baseHeroId, costumeIndex, costumeBonusJson, null);
        }

        private static Resolution unresolved(String reason) {
            return new Resolution(false, null, null, null, null, null, null, null, null, null, reason);
        }

        private boolean isResolved() {
            return reason == null;
        }
    }

    private record ParsedLocalizedDescription(
            String skillName,
            String skillDescription
    ) {
    }
}
