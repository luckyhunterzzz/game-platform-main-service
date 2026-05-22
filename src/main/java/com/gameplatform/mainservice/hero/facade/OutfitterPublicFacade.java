package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.dto.response.OutfitterForecastResponse;
import com.gameplatform.mainservice.hero.dto.response.OutfitterPageResponse;
import com.gameplatform.mainservice.hero.service.OutfitterPublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class OutfitterPublicFacade {

    private final OutfitterPublicService outfitterPublicService;

    public OutfitterPageResponse getAvailableHeroes(int page, int size, HeroLanguage language) {
        return outfitterPublicService.getAvailableHeroes(page, size, language);
    }

    public OutfitterForecastResponse getForecast(LocalDate targetDate, LocalDate previousEventDate, HeroLanguage language) {
        return outfitterPublicService.getForecast(targetDate, previousEventDate, language);
    }
}
