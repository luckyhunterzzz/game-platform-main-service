package com.gameplatform.mainservice.hero.service.importer;

import com.gameplatform.mainservice.hero.domain.entity.AlphaTalent;
import com.gameplatform.mainservice.hero.domain.entity.Family;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.hero.repository.AlphaTalentRepository;
import com.gameplatform.mainservice.hero.repository.FamilyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class HeroImportDictionaryLookupService {

    private static final Pattern LEGENDS_YEAR_PATTERN = Pattern.compile("^legends\\s+(\\d{4})(?:\\s+family)?$", Pattern.CASE_INSENSITIVE);

    private final FamilyRepository familyRepository;
    private final AlphaTalentRepository alphaTalentRepository;
    private final HeroImportSourceValueNormalizer normalizer;

    public HeroImportDictionaryLookup buildLookup() {
        return new HeroImportDictionaryLookup(
                buildFamilyAliasMap(familyRepository.findAll()),
                buildAliasMap(alphaTalentRepository.findAll(), AlphaTalent::getId, AlphaTalent::getNameJson)
        );
    }

    private Map<String, Long> buildFamilyAliasMap(List<Family> families) {
        Map<String, Long> aliases = buildAliasMap(families, Family::getId, Family::getNameJson);
        for (Family family : families) {
            LocalizedTextJson nameJson = family.getNameJson();
            if (nameJson == null) {
                continue;
            }
            putDerivedFamilyAliases(aliases, nameJson.en(), family.getId());
        }
        return aliases;
    }

    private void putDerivedFamilyAliases(Map<String, Long> aliases, String englishName, Long familyId) {
        String normalizedEnglishName = trimToNull(englishName);
        if (normalizedEnglishName == null) {
            return;
        }

        Matcher legendsMatcher = LEGENDS_YEAR_PATTERN.matcher(normalizedEnglishName);
        if (legendsMatcher.matches()) {
            putAlias(aliases, "hotm" + legendsMatcher.group(1), familyId);
            return;
        }

        String withoutFamilyWord = normalizedEnglishName
                .replaceAll("(?i)\\s+family\\b", "")
                .trim();

        putAlias(aliases, withoutFamilyWord, familyId);
        putAlias(aliases, withoutFamilyWord.replaceAll("\\s+", "_"), familyId);
    }

    private <T> Map<String, Long> buildAliasMap(
            List<T> items,
            Function<T, Long> idExtractor,
            Function<T, LocalizedTextJson> nameExtractor
    ) {
        Map<String, Long> aliases = new HashMap<>();
        for (T item : items) {
            LocalizedTextJson nameJson = nameExtractor.apply(item);
            Long id = idExtractor.apply(item);
            putAlias(aliases, nameJson != null ? nameJson.en() : null, id);
            putAlias(aliases, nameJson != null ? nameJson.ru() : null, id);
        }
        return aliases;
    }

    private void putAlias(Map<String, Long> aliases, String rawAlias, Long id) {
        String normalizedAlias = normalizer.normalize(rawAlias);
        if (normalizedAlias != null) {
            aliases.putIfAbsent(normalizedAlias, id);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
