package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.converter.AlphaTalentResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.AlphaTalent;
import com.gameplatform.mainservice.hero.dto.request.AlphaTalentUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.AlphaTalentResponse;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.repository.AlphaTalentRepository;
import com.gameplatform.mainservice.hero.validation.HeroValidator;
import com.gameplatform.mainservice.media.validation.ImageReferenceValidator;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlphaTalentService {

    private final AlphaTalentRepository repository;
    private final DictionaryCatalogSupport catalogSupport;
    private final HeroValidator heroValidator;
    private final ImageReferenceValidator imageReferenceValidator;
    private final AlphaTalentResponseConverter converter;

    public List<AlphaTalentResponse> getAll() {
        return converter.toResponseList(catalogSupport.sortLocalized(repository.findAll(), AlphaTalent::getNameJson));
    }

    public CatalogPageResponse<AlphaTalentResponse> getPage(int page, int size, String search) {
        return CatalogPageResponse.from(
                catalogSupport.pageLocalized(repository.findAll(), search, page, size, AlphaTalent::getNameJson)
                        .map(converter::toResponse)
        );
    }

    public AlphaTalentResponse getById(Long id) {
        return converter.toResponse(getEntityById(id));
    }

    public AlphaTalentResponse create(AlphaTalentUpsertRequest request) {
        String normalizedRu = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().ru() : null);
        String normalizedEn = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().en() : null);
        heroValidator.validateDuplicateDictionaryName(
                "Alpha talent",
                () -> repository.existsDuplicateLocalizedName(normalizedRu, normalizedEn, null)
        );
        imageReferenceValidator.validate(request.imageBucket(), request.imageObjectKey());

        AlphaTalent entity = AlphaTalent.builder()
                .nameJson(request.nameJson())
                .descriptionJson(request.descriptionJson())
                .imageBucket(request.imageBucket())
                .imageObjectKey(request.imageObjectKey())
                .build();

        return converter.toResponse(repository.save(entity));
    }

    public AlphaTalentResponse update(Long id, AlphaTalentUpsertRequest request) {
        AlphaTalent entity = getEntityById(id);
        String normalizedRu = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().ru() : null);
        String normalizedEn = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().en() : null);
        heroValidator.validateDuplicateDictionaryName(
                "Alpha talent",
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
            throw new NotFoundException("AlphaTalent not found: " + id);
        }
        repository.deleteById(id);
    }

    private AlphaTalent getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("AlphaTalent not found: " + id));
    }
}

