package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.domain.entity.AlphaTalent;
import com.gameplatform.mainservice.hero.dto.request.AlphaTalentUpsertRequest;
import com.gameplatform.mainservice.hero.repository.AlphaTalentRepository;
import com.gameplatform.mainservice.hero.validation.HeroValidator;
import com.gameplatform.mainservice.media.validation.ImageReferenceValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlphaTalentService {

    private final AlphaTalentRepository repository;
    private final DictionaryCatalogSupport catalogSupport;
    private final HeroValidator heroValidator;
    private final ImageReferenceValidator imageReferenceValidator;

    public List<AlphaTalent> getAll() {
        return catalogSupport.sortLocalized(repository.findAll(), AlphaTalent::getNameJson);
    }

    public Page<AlphaTalent> getPage(int page, int size, String search) {
        return catalogSupport.pageLocalized(repository.findAll(), search, page, size, AlphaTalent::getNameJson);
    }

    public AlphaTalent getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("AlphaTalent not found: " + id));
    }

    public AlphaTalent create(AlphaTalentUpsertRequest request) {
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

        return repository.save(entity);
    }

    public AlphaTalent update(Long id, AlphaTalentUpsertRequest request) {
        AlphaTalent entity = getById(id);
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

        return repository.save(entity);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("AlphaTalent not found: " + id);
        }
        repository.deleteById(id);
    }
}
