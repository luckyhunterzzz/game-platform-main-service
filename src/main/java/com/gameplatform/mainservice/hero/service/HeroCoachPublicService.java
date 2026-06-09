package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.converter.HeroPublicResponseConverter;
import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.dto.response.HeroCoachForecastResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroCoachHeroResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroCoachPageResponse;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HeroCoachPublicService {

    private static final String HERO_COACH_TITLE_FRAGMENT = "Hero Coach";
    private static final int HERO_COACH_DAYS = 730;

    private final HeroRepository heroRepository;
    private final HeroPublicResponseConverter heroPublicResponseConverter;
    private final Clock clock;
    private final HeroEventForecastSupport forecastSupport;

    public HeroCoachPageResponse getAvailableHeroes(int page, int size, HeroLanguage language) {
        int normalizedPage = forecastSupport.normalizePage(page);
        int normalizedSize = forecastSupport.normalizePageSize(size, 24);
        LocalDate today = LocalDate.now(clock);
        LocalDate eligibleReleaseDate = today.minusDays(HERO_COACH_DAYS);

        Page<HeroCoachHeroResponse> heroPage = heroRepository.findReadyHeroCoachHeroes(
                language.getJsonKey(),
                eligibleReleaseDate,
                PageRequest.of(normalizedPage, normalizedSize)
        ).map(heroPublicResponseConverter::toHeroCoachResponse);

        return new HeroCoachPageResponse(
                forecastSupport.resolveSuggestedPreviousEventDate(HERO_COACH_TITLE_FRAGMENT),
                heroPage.getContent(),
                heroPage.getNumber(),
                heroPage.getSize(),
                heroPage.getTotalElements(),
                heroPage.getTotalPages(),
                heroPage.hasNext()
        );
    }

    public HeroCoachForecastResponse getForecast(LocalDate targetDate, LocalDate previousEventDate, HeroLanguage language) {
        LocalDate suggestedPreviousEventDate = forecastSupport.resolveSuggestedPreviousEventDate(HERO_COACH_TITLE_FRAGMENT);
        LocalDate effectivePreviousEventDate = forecastSupport.resolveEffectivePreviousEventDate(
                previousEventDate,
                suggestedPreviousEventDate
        );
        forecastSupport.validateForecastDates(targetDate, effectivePreviousEventDate);
        LocalDate previousComparisonDate = forecastSupport.resolvePreviousComparisonDate(
                targetDate,
                effectivePreviousEventDate
        );

        List<HeroCoachHeroResponse> newlyAvailableHeroes = heroRepository.findReadyHeroCoachHeroesReleasedBetween(
                        language.getJsonKey(),
                        previousComparisonDate.minusDays(HERO_COACH_DAYS),
                        targetDate.minusDays(HERO_COACH_DAYS)
                ).stream()
                .map(heroPublicResponseConverter::toHeroCoachResponse)
                .toList();

        return new HeroCoachForecastResponse(
                suggestedPreviousEventDate,
                previousComparisonDate,
                targetDate,
                newlyAvailableHeroes
        );
    }
}
