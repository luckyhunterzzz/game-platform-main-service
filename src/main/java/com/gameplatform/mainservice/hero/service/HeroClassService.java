package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.converter.HeroClassResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.HeroClass;
import com.gameplatform.mainservice.hero.dto.request.HeroClassUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroClassResponse;
import com.gameplatform.mainservice.hero.repository.HeroClassRepository;
import com.gameplatform.mainservice.hero.validation.HeroValidator;
import com.gameplatform.mainservice.media.validation.ImageReferenceValidator;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HeroClassService {

    private final HeroClassRepository repository;
    private final DictionaryCatalogSupport catalogSupport;
    private final HeroValidator heroValidator;
    private final ImageReferenceValidator imageReferenceValidator;
    private final HeroClassResponseConverter converter;

    public List<HeroClassResponse> getAll() {
        return converter.toResponseList(catalogSupport.sortLocalized(repository.findAll(), HeroClass::getNameJson));
    }

    public CatalogPageResponse<HeroClassResponse> getPage(int page, int size, String search) {
        return CatalogPageResponse.from(
                catalogSupport.pageLocalized(repository.findAll(), search, page, size, HeroClass::getNameJson)
                        .map(converter::toResponse)
        );
    }

    public HeroClassResponse getById(Long id) {
        return converter.toResponse(getEntityById(id));
    }

    public HeroClassResponse create(HeroClassUpsertRequest request) {
        String normalizedRu = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().ru() : null);
        String normalizedEn = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().en() : null);
        heroValidator.validateDuplicateDictionaryName(
                "Hero class",
                () -> repository.existsDuplicateLocalizedName(normalizedRu, normalizedEn, null)
        );
        imageReferenceValidator.validate(request.imageBucket(), request.imageObjectKey());

        HeroClass entity = HeroClass.builder()
                .nameJson(request.nameJson())
                .baseNameJson(request.baseNameJson())
                .baseDescriptionJson(request.baseDescriptionJson())
                .masterNameJson(request.masterNameJson())
                .masterDescriptionJson(request.masterDescriptionJson())
                .imageBucket(request.imageBucket())
                .imageObjectKey(request.imageObjectKey())
                .build();

        return converter.toResponse(repository.save(entity));
    }

    public HeroClassResponse update(Long id, HeroClassUpsertRequest request) {
        HeroClass entity = getEntityById(id);
        String normalizedRu = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().ru() : null);
        String normalizedEn = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().en() : null);
        heroValidator.validateDuplicateDictionaryName(
                "Hero class",
                () -> repository.existsDuplicateLocalizedName(normalizedRu, normalizedEn, id)
        );
        imageReferenceValidator.validate(request.imageBucket(), request.imageObjectKey());

        entity.setNameJson(request.nameJson());
        entity.setBaseNameJson(request.baseNameJson());
        entity.setBaseDescriptionJson(request.baseDescriptionJson());
        entity.setMasterNameJson(request.masterNameJson());
        entity.setMasterDescriptionJson(request.masterDescriptionJson());
        entity.setImageBucket(request.imageBucket());
        entity.setImageObjectKey(request.imageObjectKey());

        return converter.toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("HeroClass not found: " + id);
        }
        repository.deleteById(id);
    }

    private HeroClass getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("HeroClass not found: " + id));
    }
}

