package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.exception.exceptions.DictionaryItemInUseException;
import com.gameplatform.mainservice.hero.converter.PassiveSkillResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.PassiveSkill;
import com.gameplatform.mainservice.hero.dto.request.PassiveSkillUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.dto.response.PassiveSkillResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroUsageReferenceResponse;
import com.gameplatform.mainservice.hero.repository.HeroPassiveSkillRepository;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import com.gameplatform.mainservice.hero.repository.PassiveSkillRepository;
import com.gameplatform.mainservice.hero.validation.HeroValidator;
import com.gameplatform.mainservice.media.validation.ImageReferenceValidator;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PassiveSkillService {

    private final PassiveSkillRepository repository;
    private final HeroPassiveSkillRepository heroPassiveSkillRepository;
    private final HeroRepository heroRepository;
    private final DictionaryCatalogSupport catalogSupport;
    private final HeroValidator heroValidator;
    private final ImageReferenceValidator imageReferenceValidator;
    private final PassiveSkillResponseConverter converter;

    public List<PassiveSkillResponse> getAll() {
        return converter.toResponseList(catalogSupport.sortLocalized(repository.findAll(), PassiveSkill::getNameJson));
    }

    public CatalogPageResponse<PassiveSkillResponse> getPage(int page, int size, String search) {
        return CatalogPageResponse.from(
                catalogSupport.pageLocalized(repository.findAll(), search, page, size, PassiveSkill::getNameJson)
                        .map(converter::toResponse)
        );
    }

    public PassiveSkillResponse getById(Long id) {
        return converter.toResponse(getEntityById(id));
    }

    public PassiveSkillResponse create(PassiveSkillUpsertRequest request) {
        String normalizedRu = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().ru() : null);
        String normalizedEn = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().en() : null);
        heroValidator.validateDuplicateDictionaryName(
                "Passive skill",
                () -> repository.existsDuplicateLocalizedName(normalizedRu, normalizedEn, null)
        );
        imageReferenceValidator.validate(request.imageBucket(), request.imageObjectKey());

        PassiveSkill entity = PassiveSkill.builder()
                .nameJson(request.nameJson())
                .descriptionJson(request.descriptionJson())
                .imageBucket(request.imageBucket())
                .imageObjectKey(request.imageObjectKey())
                .build();

        return converter.toResponse(repository.save(entity));
    }

    public PassiveSkillResponse update(Long id, PassiveSkillUpsertRequest request) {
        PassiveSkill entity = getEntityById(id);
        String normalizedRu = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().ru() : null);
        String normalizedEn = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().en() : null);
        heroValidator.validateDuplicateDictionaryName(
                "Passive skill",
                () -> repository.existsDuplicateLocalizedName(normalizedRu, normalizedEn, id)
        );
        imageReferenceValidator.validate(request.imageBucket(), request.imageObjectKey());

        entity.setNameJson(request.nameJson());
        entity.setDescriptionJson(request.descriptionJson());
        entity.setImageBucket(request.imageBucket());
        entity.setImageObjectKey(request.imageObjectKey());

        return converter.toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("PassiveSkill not found: " + id);
        }

        if (heroPassiveSkillRepository.existsByIdPassiveSkillId(id)) {
            throw new DictionaryItemInUseException(
                    "Passive skill is used by one or more heroes and cannot be deleted",
                    catalogSupport.sortLocalized(
                                    heroRepository.findAllByPassiveSkillId(id),
                                    hero -> hero.getNameJson()
                            ).stream()
                            .map(hero -> new HeroUsageReferenceResponse(
                                    hero.getId(),
                                    hero.getSlug(),
                                    hero.getNameJson(),
                                    hero.getStatus().name()
                            ))
                            .toList()
            );
        }

        repository.deleteById(id);
    }

    private PassiveSkill getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("PassiveSkill not found: " + id));
    }
}

