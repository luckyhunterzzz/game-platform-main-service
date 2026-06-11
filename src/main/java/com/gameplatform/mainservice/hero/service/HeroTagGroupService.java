package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.config.PublicCacheEvictionService;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import com.gameplatform.mainservice.hero.converter.HeroTagGroupResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.HeroTag;
import com.gameplatform.mainservice.hero.domain.entity.HeroTagGroup;
import com.gameplatform.mainservice.hero.dto.request.HeroTagGroupUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroTagGroupResponse;
import com.gameplatform.mainservice.hero.repository.HeroTagGroupRepository;
import com.gameplatform.mainservice.hero.repository.HeroTagRepository;
import com.gameplatform.mainservice.hero.validation.HeroValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HeroTagGroupService {

    private final HeroTagGroupRepository groupRepository;
    private final HeroTagRepository tagRepository;
    private final DictionaryCatalogSupport catalogSupport;
    private final HeroValidator heroValidator;
    private final HeroTagGroupResponseConverter converter;
    private final PublicCacheEvictionService publicCacheEvictionService;

    public List<HeroTagGroupResponse> getAll() {
        List<HeroTagGroup> groups = catalogSupport.sortLocalized(groupRepository.findAll(), HeroTagGroup::getNameJson);
        return converter.toResponseList(groups, tagRepository.findAll());
    }

    public CatalogPageResponse<HeroTagGroupResponse> getPage(int page, int size, String search) {
        List<HeroTagGroup> allGroups = groupRepository.findAll();
        List<HeroTag> allTags = tagRepository.findAll();
        return CatalogPageResponse.from(
                catalogSupport.pageLocalized(allGroups, search, page, size, HeroTagGroup::getNameJson)
                        .map(group -> converter.toResponse(group, allTags))
        );
    }

    public HeroTagGroupResponse getById(Long id) {
        HeroTagGroup group = getEntityById(id);
        return converter.toResponse(group, tagRepository.findAll());
    }

    public HeroTagGroupResponse create(HeroTagGroupUpsertRequest request) {
        validateUpsert(request, null);
        HeroTagGroup group = HeroTagGroup.builder().active(true).build();
        applyUpsert(group, request);
        HeroTagGroup saved = groupRepository.save(group);
        syncTags(saved.getId(), request.tagIds());
        publicCacheEvictionService.evictHeroCaches();
        return getById(saved.getId());
    }

    public HeroTagGroupResponse update(Long id, HeroTagGroupUpsertRequest request) {
        HeroTagGroup group = getEntityById(id);
        validateUpsert(request, id);
        applyUpsert(group, request);
        groupRepository.save(group);
        syncTags(group.getId(), request.tagIds());
        publicCacheEvictionService.evictHeroCaches();
        return getById(group.getId());
    }

    public void delete(Long id) {
        HeroTagGroup group = getEntityById(id);
        if (tagRepository.existsByGroupId(id)) {
            throw new IllegalStateException("Hero tag group is used by one or more tags and cannot be deleted");
        }
        groupRepository.delete(group);
        publicCacheEvictionService.evictHeroCaches();
    }

    private HeroTagGroup getEntityById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("HeroTagGroup not found: " + id));
    }

    private void validateUpsert(HeroTagGroupUpsertRequest request, Long excludedId) {
        String normalizedRu = heroValidator.normalizeDictionaryName(request.nameJson().ru());
        String normalizedEn = heroValidator.normalizeDictionaryName(request.nameJson().en());
        heroValidator.validateDuplicateDictionaryName(
                "HeroTagGroup",
                () -> groupRepository.existsDuplicateLocalizedName(normalizedRu, normalizedEn, excludedId)
        );

        if (request.tagIds() != null && !request.tagIds().isEmpty()) {
            Set<Long> existingIds = tagRepository.findAllById(request.tagIds()).stream()
                    .map(HeroTag::getId)
                    .collect(java.util.stream.Collectors.toSet());
            for (Long tagId : request.tagIds()) {
                if (tagId != null && !existingIds.contains(tagId)) {
                    throw new NotFoundException("HeroTag not found: " + tagId);
                }
            }
        }
    }

    private void applyUpsert(HeroTagGroup entity, HeroTagGroupUpsertRequest request) {
        entity.setCode(buildCode(request.nameJson()));
        entity.setNameJson(request.nameJson());
        entity.setDescriptionJson(request.descriptionJson());
        entity.setActive(true);
    }

    private void syncTags(Long groupId, List<Long> tagIds) {
        if (tagIds == null) {
            return;
        }

        List<HeroTag> existingGroupTags = tagRepository.findAllByGroupId(groupId);
        for (HeroTag tag : existingGroupTags) {
            if (!tagIds.contains(tag.getId())) {
                // Keep current tags as-is if the client does not explicitly manage full group membership.
                // Reassignment is handled by the tag editor.
            }
        }

        List<HeroTag> tagsToAssign = tagRepository.findAllById(tagIds);
        for (HeroTag tag : tagsToAssign) {
            if (!groupId.equals(tag.getGroupId())) {
                tag.setGroupId(groupId);
            }
        }
        tagRepository.saveAll(tagsToAssign);
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
