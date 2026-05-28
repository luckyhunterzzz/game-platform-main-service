package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.hero.converter.HeroExpertOpinionResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.HeroExpertOpinion;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.hero.dto.request.HeroExpertOpinionUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroExpertOpinionAdminResponse;
import com.gameplatform.mainservice.hero.repository.HeroExpertOpinionRepository;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HeroExpertOpinionAdminService {

    private final Clock clock;
    private final HeroRepository heroRepository;
    private final HeroExpertOpinionRepository heroExpertOpinionRepository;
    private final HeroExpertOpinionResponseConverter converter;

    public List<HeroExpertOpinionAdminResponse> getAllByHeroId(Long heroId) {
        requireHero(heroId);
        return heroExpertOpinionRepository.findAllByHeroIdOrdered(heroId).stream()
                .map(converter::toAdminResponse)
                .toList();
    }

    @Transactional
    public HeroExpertOpinionAdminResponse create(Long heroId, HeroExpertOpinionUpsertRequest request) {
        requireHero(heroId);
        validateRequest(request);

        OffsetDateTime now = OffsetDateTime.now(clock);

        HeroExpertOpinion entity = HeroExpertOpinion.builder()
                .heroId(heroId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        applyUpsert(entity, request);
        return converter.toAdminResponse(heroExpertOpinionRepository.save(entity));
    }

    @Transactional
    public HeroExpertOpinionAdminResponse update(Long heroId, Long opinionId, HeroExpertOpinionUpsertRequest request) {
        requireHero(heroId);
        validateRequest(request);

        HeroExpertOpinion entity = heroExpertOpinionRepository.findByIdAndHeroId(opinionId, heroId)
                .orElseThrow(() -> new NotFoundException("Hero expert opinion not found: " + opinionId));

        entity.setUpdatedAt(OffsetDateTime.now(clock));
        applyUpsert(entity, request);

        return converter.toAdminResponse(heroExpertOpinionRepository.save(entity));
    }

    @Transactional
    public void delete(Long heroId, Long opinionId) {
        requireHero(heroId);

        HeroExpertOpinion entity = heroExpertOpinionRepository.findByIdAndHeroId(opinionId, heroId)
                .orElseThrow(() -> new NotFoundException("Hero expert opinion not found: " + opinionId));

        heroExpertOpinionRepository.delete(entity);
    }

    @Transactional
    public void deleteAllByHeroId(Long heroId) {
        heroExpertOpinionRepository.deleteAllByHeroId(heroId);
    }

    private void applyUpsert(HeroExpertOpinion entity, HeroExpertOpinionUpsertRequest request) {
        entity.setAuthorName(normalizeRequired(request.authorName()));
        entity.setSourceUrl(normalizeOptional(request.sourceUrl()));
        entity.setSourceTitle(normalizeOptional(request.sourceTitle()));
        entity.setSourceType(request.sourceType());
        entity.setContentJson(normalizeContent(request.contentJson()));
        entity.setPublished(Boolean.TRUE.equals(request.isPublished()));
        entity.setPublishedAt(request.publishedAt());
    }

    private void validateRequest(HeroExpertOpinionUpsertRequest request) {
        LocalizedTextJson content = request.contentJson();
        if (content == null || isBlank(content.ru()) && isBlank(content.en())) {
            throw new BusinessValidationException("Hero expert opinion content must contain at least one locale");
        }

        if (Boolean.TRUE.equals(request.isPublished()) && request.publishedAt() == null) {
            throw new BusinessValidationException("Published hero expert opinion must have publishedAt");
        }
    }

    private void requireHero(Long heroId) {
        if (!heroRepository.existsById(heroId)) {
            throw new NotFoundException("Hero not found: " + heroId);
        }
    }

    private LocalizedTextJson normalizeContent(LocalizedTextJson content) {
        if (content == null) {
            return null;
        }

        return new LocalizedTextJson(
                normalizeOptional(content.ru()),
                normalizeOptional(content.en())
        );
    }

    private String normalizeRequired(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new BusinessValidationException("Hero expert opinion authorName must not be blank");
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

