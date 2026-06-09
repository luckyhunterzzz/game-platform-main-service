package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.config.CacheNames;
import com.gameplatform.mainservice.hero.converter.HeroPublicResponseConverter;
import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.dto.response.OutfitterForecastResponse;
import com.gameplatform.mainservice.hero.dto.response.OutfitterHeroResponse;
import com.gameplatform.mainservice.hero.dto.response.OutfitterPageResponse;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import com.gameplatform.mainservice.settings.service.HeroPublicVisibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutfitterPublicService {

    private static final String VISITING_OUTFITTER_TITLE_FRAGMENT = "Visiting Outfitter";
    private static final long VISITING_OUTFITTER_DAYS_EXCLUDING_RELEASE_DATE = 549L;

    private final HeroRepository heroRepository;
    private final HeroPublicResponseConverter heroPublicResponseConverter;
    private final Clock clock;
    private final HeroEventForecastSupport forecastSupport;
    private final HeroPublicVisibilityService heroPublicVisibilityService;

    @Cacheable(cacheNames = CacheNames.PUBLIC_OUTFITTER_PAGE)
    public OutfitterPageResponse getAvailableHeroes(int page, int size, HeroLanguage language) {
        int normalizedPage = forecastSupport.normalizePage(page);
        int normalizedSize = forecastSupport.normalizePageSize(size, 24);
        LocalDate today = LocalDate.now(clock);
        LocalDate eligibleReleaseDate = today.minusDays(VISITING_OUTFITTER_DAYS_EXCLUDING_RELEASE_DATE);
        boolean includeDrafts = heroPublicVisibilityService.isDraftVisibleInPublicCatalog();

        Page<OutfitterHeroResponse> heroPage = heroRepository.findReadyOutfitterHeroes(
                language.getJsonKey(),
                eligibleReleaseDate,
                includeDrafts,
                PageRequest.of(normalizedPage, normalizedSize)
        ).map(heroPublicResponseConverter::toOutfitterResponse);

        return new OutfitterPageResponse(
                forecastSupport.resolveSuggestedPreviousEventDate(VISITING_OUTFITTER_TITLE_FRAGMENT),
                heroPage.getContent(),
                heroPage.getNumber(),
                heroPage.getSize(),
                heroPage.getTotalElements(),
                heroPage.getTotalPages(),
                heroPage.hasNext()
        );
    }

    @Cacheable(cacheNames = CacheNames.PUBLIC_OUTFITTER_FORECAST)
    public OutfitterForecastResponse getForecast(LocalDate targetDate, LocalDate previousEventDate, HeroLanguage language) {
        LocalDate suggestedPreviousEventDate = forecastSupport.resolveSuggestedPreviousEventDate(VISITING_OUTFITTER_TITLE_FRAGMENT);
        LocalDate effectivePreviousEventDate = forecastSupport.resolveEffectivePreviousEventDate(
                previousEventDate,
                suggestedPreviousEventDate
        );
        forecastSupport.validateForecastDates(targetDate, effectivePreviousEventDate);
        boolean includeDrafts = heroPublicVisibilityService.isDraftVisibleInPublicCatalog();
        LocalDate previousComparisonDate = forecastSupport.resolvePreviousComparisonDate(
                targetDate,
                effectivePreviousEventDate
        );

        List<OutfitterHeroResponse> newlyAvailableHeroes = heroRepository.findReadyOutfitterHeroesReleasedBetween(
                        language.getJsonKey(),
                        previousComparisonDate.minusDays(VISITING_OUTFITTER_DAYS_EXCLUDING_RELEASE_DATE),
                        targetDate.minusDays(VISITING_OUTFITTER_DAYS_EXCLUDING_RELEASE_DATE),
                        includeDrafts
                ).stream()
                .map(heroPublicResponseConverter::toOutfitterResponse)
                .toList();

        return new OutfitterForecastResponse(
                suggestedPreviousEventDate,
                previousComparisonDate,
                targetDate,
                newlyAvailableHeroes
        );
    }
}
