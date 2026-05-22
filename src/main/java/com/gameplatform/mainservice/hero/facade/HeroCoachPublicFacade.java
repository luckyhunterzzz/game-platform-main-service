package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.dto.response.HeroCoachForecastResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroCoachPageResponse;
import com.gameplatform.mainservice.hero.service.HeroCoachPublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class HeroCoachPublicFacade {

    private final HeroCoachPublicService heroCoachPublicService;

    public HeroCoachPageResponse getAvailableHeroes(int page, int size, HeroLanguage language) {
        return heroCoachPublicService.getAvailableHeroes(page, size, language);
    }

    public HeroCoachForecastResponse getForecast(LocalDate targetDate, LocalDate previousEventDate, HeroLanguage language) {
        return heroCoachPublicService.getForecast(targetDate, previousEventDate, language);
    }
}
