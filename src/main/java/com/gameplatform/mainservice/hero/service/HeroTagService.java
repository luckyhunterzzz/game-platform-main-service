package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.config.PublicCacheEvictionService;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import com.gameplatform.mainservice.hero.converter.HeroTagResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.HeroTag;
import com.gameplatform.mainservice.hero.domain.entity.HeroTagGroup;
import com.gameplatform.mainservice.hero.dto.request.HeroTagUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroTagResponse;
import com.gameplatform.mainservice.hero.repository.HeroTagGroupRepository;
import com.gameplatform.mainservice.hero.repository.HeroTagLinkRepository;
import com.gameplatform.mainservice.hero.repository.HeroTagRepository;
import com.gameplatform.mainservice.hero.validation.HeroValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HeroTagService {

    private final HeroTagRepository tagRepository;
    private final HeroTagGroupRepository groupRepository;
    private final HeroTagLinkRepository tagLinkRepository;
    private final DictionaryCatalogSupport catalogSupport;
    private final HeroValidator heroValidator;
    private final HeroTagResponseConverter converter;
    private final PublicCacheEvictionService publicCacheEvictionService;

    public List<HeroTagResponse> getAll() {
        List<HeroTag> tags = catalogSupport.sortLocalized(tagRepository.findAll(), HeroTag::getNameJson);
        return converter.toResponseList(tags, groupRepository.findAll());
    }

    public CatalogPageResponse<HeroTagResponse> getPage(int page, int size, String search) {
        List<HeroTag> allTags = tagRepository.findAll();
        List<HeroTagGroup> allGroups = groupRepository.findAll();
        return CatalogPageResponse.from(
                catalogSupport.pageLocalized(allTags, search, page, size, HeroTag::getNameJson)
                        .map(tag -> converter.toResponse(
                                tag,
                                allGroups.stream().filter(group -> group.getId().equals(tag.getGroupId())).findFirst().orElse(null)
                        ))
        );
    }

    public HeroTagResponse getById(Long id) {
        HeroTag tag = getEntityById(id);
        HeroTagGroup group = groupRepository.findById(tag.getGroupId())
                .orElseThrow(() -> new NotFoundException("HeroTagGroup not found: " + tag.getGroupId()));
        return converter.toResponse(tag, group);
    }

    public HeroTagResponse create(HeroTagUpsertRequest request) {
        validateUpsert(request, null);
        HeroTag tag = HeroTag.builder().active(true).build();
        applyUpsert(tag, request);
        HeroTag saved = tagRepository.save(tag);
        publicCacheEvictionService.evictHeroCaches();
        return getById(saved.getId());
    }

    public HeroTagResponse update(Long id, HeroTagUpsertRequest request) {
        HeroTag tag = getEntityById(id);
        validateUpsert(request, id);
        applyUpsert(tag, request);
        tagRepository.save(tag);
        publicCacheEvictionService.evictHeroCaches();
        return getById(tag.getId());
    }

    public void delete(Long id) {
        HeroTag tag = getEntityById(id);
        if (tagLinkRepository.existsByIdTagId(id)) {
            throw new IllegalStateException("Hero tag is used by one or more heroes and cannot be deleted");
        }
        tagRepository.delete(tag);
        publicCacheEvictionService.evictHeroCaches();
    }

    private HeroTag getEntityById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("HeroTag not found: " + id));
    }

    private void validateUpsert(HeroTagUpsertRequest request, Long excludedId) {
        String normalizedRu = heroValidator.normalizeDictionaryName(request.nameJson().ru());
        String normalizedEn = heroValidator.normalizeDictionaryName(request.nameJson().en());
        heroValidator.validateDuplicateDictionaryName(
                "HeroTag",
                () -> tagRepository.existsDuplicateLocalizedName(normalizedRu, normalizedEn, excludedId)
        );
        if (!groupRepository.existsById(request.groupId())) {
            throw new NotFoundException("HeroTagGroup not found: " + request.groupId());
        }
    }

    private void applyUpsert(HeroTag entity, HeroTagUpsertRequest request) {
        entity.setCode(buildCode(request.nameJson()));
        entity.setNameJson(request.nameJson());
        entity.setDescriptionJson(request.descriptionJson());
        entity.setGroupId(request.groupId());
        entity.setActive(true);
    }

    private String buildCode(com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson nameJson) {
        String source = nameJson == null ? null : (nameJson.en() != null && !nameJson.en().isBlank() ? nameJson.en() : nameJson.ru());
        if (source == null) {
            return null;
        }
        return source.trim().toLowerCase()
                .replaceAll("[^a-z0-9а-яё\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }
}
