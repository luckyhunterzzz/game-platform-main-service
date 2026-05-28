package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.domain.entity.Family;
import com.gameplatform.mainservice.hero.dto.request.FamilyUpsertRequest;
import com.gameplatform.mainservice.hero.repository.FamilyRepository;
import com.gameplatform.mainservice.hero.validation.HeroValidator;
import com.gameplatform.mainservice.media.validation.ImageReferenceValidator;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final DictionaryCatalogSupport catalogSupport;
    private final HeroValidator heroValidator;
    private final ImageReferenceValidator imageReferenceValidator;

    public List<Family> getAll() {
        return catalogSupport.sortLocalized(familyRepository.findAll(), Family::getNameJson);
    }

    public Page<Family> getPage(int page, int size, String search) {
        return catalogSupport.pageLocalized(familyRepository.findAll(), search, page, size, Family::getNameJson);
    }

    public Family getById(Long id) {
        return familyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Family not found: " + id));
    }

    public Family create(FamilyUpsertRequest request) {
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

        return familyRepository.save(family);
    }

    public Family update(Long id, FamilyUpsertRequest request) {
        Family family = getById(id);
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

        return familyRepository.save(family);
    }

    public void delete(Long id) {
        if (!familyRepository.existsById(id)) {
            throw new NotFoundException("Family not found: " + id);
        }
        familyRepository.deleteById(id);
    }
}

