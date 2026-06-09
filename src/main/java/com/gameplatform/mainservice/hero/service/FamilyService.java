package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.config.PublicCacheEvictionService;
import com.gameplatform.mainservice.hero.converter.FamilyResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.Family;
import com.gameplatform.mainservice.hero.dto.request.FamilyUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.dto.response.FamilyResponse;
import com.gameplatform.mainservice.hero.repository.FamilyRepository;
import com.gameplatform.mainservice.hero.validation.HeroValidator;
import com.gameplatform.mainservice.media.validation.ImageReferenceValidator;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final DictionaryCatalogSupport catalogSupport;
    private final HeroValidator heroValidator;
    private final ImageReferenceValidator imageReferenceValidator;
    private final FamilyResponseConverter converter;
    private final PublicCacheEvictionService publicCacheEvictionService;

    public List<FamilyResponse> getAll() {
        return converter.toResponseList(catalogSupport.sortLocalized(familyRepository.findAll(), Family::getNameJson));
    }

    public CatalogPageResponse<FamilyResponse> getPage(int page, int size, String search) {
        return CatalogPageResponse.from(
                catalogSupport.pageLocalized(familyRepository.findAll(), search, page, size, Family::getNameJson)
                        .map(converter::toResponse)
        );
    }

    public FamilyResponse getById(Long id) {
        return converter.toResponse(getEntityById(id));
    }

    public FamilyResponse create(FamilyUpsertRequest request) {
        validateUpsert(request, null);
        Family family = Family.builder().build();
        applyUpsert(family, request);

        FamilyResponse response = converter.toResponse(familyRepository.save(family));
        publicCacheEvictionService.evictHeroCaches();
        return response;
    }

    public FamilyResponse update(Long id, FamilyUpsertRequest request) {
        Family family = getEntityById(id);
        validateUpsert(request, id);
        applyUpsert(family, request);

        FamilyResponse response = converter.toResponse(familyRepository.save(family));
        publicCacheEvictionService.evictHeroCaches();
        return response;
    }

    public void delete(Long id) {
        if (!familyRepository.existsById(id)) {
            throw new NotFoundException("Family not found: " + id);
        }
        familyRepository.deleteById(id);
        publicCacheEvictionService.evictHeroCaches();
    }

    private Family getEntityById(Long id) {
        return familyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Family not found: " + id));
    }

    private void validateUpsert(FamilyUpsertRequest request, Long id) {
        String normalizedRu = heroValidator.normalizeDictionaryName(request.nameJson().ru());
        String normalizedEn = heroValidator.normalizeDictionaryName(request.nameJson().en());
        heroValidator.validateDuplicateDictionaryName(
                "Family",
                () -> familyRepository.existsDuplicateLocalizedName(normalizedRu, normalizedEn, id)
        );
        imageReferenceValidator.validate(request.imageBucket(), request.imageObjectKey());
    }

    private void applyUpsert(Family entity, FamilyUpsertRequest request) {
        entity.setNameJson(request.nameJson());
        entity.setDescriptionJson(request.descriptionJson());
        entity.setImageBucket(request.imageBucket());
        entity.setImageObjectKey(request.imageObjectKey());
    }
}

