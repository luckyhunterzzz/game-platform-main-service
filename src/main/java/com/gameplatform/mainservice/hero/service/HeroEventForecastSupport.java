package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.publication.repository.PublicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class HeroEventForecastSupport {

    private final PublicationRepository publicationRepository;
    private final Clock clock;

    public int normalizePage(int page) {
        return Math.max(page, 0);
    }

    public int normalizePageSize(int size, int maxSize) {
        return clamp(size, 1, maxSize);
    }

    public LocalDate resolveSuggestedPreviousEventDate(String titleFragment) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        return publicationRepository.findLatestPublishedAtByEnglishTitleContainingSince(
                        titleFragment,
                        now.minusMonths(3)
                )
                .map(this::toLocalDate)
                .orElse(null);
    }

    public LocalDate resolveEffectivePreviousEventDate(LocalDate previousEventDate, LocalDate suggestedPreviousEventDate) {
        return previousEventDate != null ? previousEventDate : suggestedPreviousEventDate;
    }

    public void validateForecastDates(LocalDate targetDate, LocalDate effectivePreviousEventDate) {
        if (targetDate == null) {
            throw new BusinessValidationException("targetDate is required");
        }

        if (effectivePreviousEventDate != null && effectivePreviousEventDate.isAfter(targetDate)) {
            throw new BusinessValidationException("previousEventDate must be before or equal to targetDate");
        }
    }

    public LocalDate resolvePreviousComparisonDate(LocalDate targetDate, LocalDate effectivePreviousEventDate) {
        return effectivePreviousEventDate != null
                ? effectivePreviousEventDate
                : targetDate.minusMonths(3);
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }

        return Math.min(value, max);

    }

    private LocalDate toLocalDate(Instant publishedAt) {
        return publishedAt.atZone(clock.getZone()).toLocalDate();
    }
}
