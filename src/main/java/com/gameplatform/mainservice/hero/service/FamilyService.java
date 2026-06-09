package com.gameplatform.mainservice.hero.service;

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
        String normalizedRu = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().ru() : null);
        String normalizedEn = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().en() : null);
        heroValidator.validateDuplicateDictionaryName(
                "Family",
                () -> familyRepository.existsDuplicateLocalizedName(normalizedRu, normalizedEn, null)
        );
        imageReferenceValidator.validate(request.imageBucket(), request.imageObjectKey());

        Family family = Family.builder()
                .nameJson(request.nameJson())
                .descriptionJson(request.descriptionJson())
                .imageBucket(request.imageBucket())
                .imageObjectKey(request.imageObjectKey())
                .build();

        return converter.toResponse(familyRepository.save(family));
    }

    public FamilyResponse update(Long id, FamilyUpsertRequest request) {
        Family family = getEntityById(id);
        String normalizedRu = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().ru() : null);
        String normalizedEn = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().en() : null);
        heroValidator.validateDuplicateDictionaryName(
                "Family",
                () -> familyRepository.existsDuplicateLocalizedName(normalizedRu, normalizedEn, id)
        );
        imageReferenceValidator.validate(request.imageBucket(), request.imageObjectKey());

        family.setNameJson(request.nameJson());
        family.setDescriptionJson(request.descriptionJson());
        family.setImageBucket(request.imageBucket());
        family.setImageObjectKey(request.imageObjectKey());

        return converter.toResponse(familyRepository.save(family));
    }

    public void delete(Long id) {
        if (!familyRepository.existsById(id)) {
            throw new NotFoundException("Family not found: " + id);
        }
        familyRepository.deleteById(id);
    }

    private Family getEntityById(Long id) {
        return familyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Family not found: " + id));
    }
}

