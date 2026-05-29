package com.gameplatform.mainservice.hero.service.importer;

import com.gameplatform.mainservice.hero.validation.HeroValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HeroImportSourceValueNormalizer {

    private final HeroValidator heroValidator;

    public String normalize(String value) {
        return heroValidator.normalizeDictionaryName(value);
    }
}
