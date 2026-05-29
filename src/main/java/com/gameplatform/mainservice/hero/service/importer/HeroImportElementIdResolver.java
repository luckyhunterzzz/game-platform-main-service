package com.gameplatform.mainservice.hero.service.importer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class HeroImportElementIdResolver {

    private static final Map<String, Long> ELEMENT_ID_BY_SOURCE_VALUE = Map.of(
            "blue", 3L,
            "red", 1L,
            "green", 2L,
            "yellow", 4L,
            "purple", 5L
    );

    private final HeroImportSourceValueNormalizer normalizer;

    public Long resolveId(String sourceValue) {
        String normalizedValue = normalizer.normalize(sourceValue);
        return normalizedValue == null ? null : ELEMENT_ID_BY_SOURCE_VALUE.get(normalizedValue);
    }
}
