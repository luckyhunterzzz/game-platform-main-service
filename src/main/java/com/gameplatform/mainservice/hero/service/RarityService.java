package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.converter.RarityResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.Rarity;
import com.gameplatform.mainservice.hero.dto.request.RarityUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.dto.response.RarityResponse;
import com.gameplatform.mainservice.hero.repository.RarityRepository;
import com.gameplatform.mainservice.hero.validation.HeroValidator;
import com.gameplatform.mainservice.media.validation.ImageReferenceValidator;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RarityService {

    private final RarityRepository rarityRepository;
    private final DictionaryCatalogSupport catalogSupport;
    private final HeroValidator heroValidator;
    private final ImageReferenceValidator imageReferenceValidator;
    private final RarityResponseConverter converter;

    public List<RarityResponse> getAll() {
        return converter.toResponseList(catalogSupport.sortLocalized(rarityRepository.findAll(), Rarity::getNameJson));
    }

    public CatalogPageResponse<RarityResponse> getPage(int page, int size, String search) {
        return CatalogPageResponse.from(
                catalogSupport.pageLocalized(rarityRepository.findAll(), search, page, size, Rarity::getNameJson)
                        .map(converter::toResponse)
        );
    }

    public RarityResponse getById(Long id) {
        return converter.toResponse(getEntityById(id));
    }

    public RarityResponse create(RarityUpsertRequest request) {
        String normalizedRu = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().ru() : null);
        String normalizedEn = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().en() : null);
        heroValidator.validateDuplicateDictionaryName(
                "Rarity",
                () -> rarityRepository.existsDuplicateLocalizedName(normalizedRu, normalizedEn, null)
        );
        imageReferenceValidator.validate(request.imageBucket(), request.imageObjectKey());

        Rarity rarity = Rarity.builder()
                .nameJson(request.nameJson())
                .stars(request.stars())
                .imageBucket(request.imageBucket())
                .imageObjectKey(request.imageObjectKey())
                .build();

        return converter.toResponse(rarityRepository.save(rarity));
    }

    public RarityResponse update(Long id, RarityUpsertRequest request) {
        Rarity rarity = getEntityById(id);
        String normalizedRu = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().ru() : null);
        String normalizedEn = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().en() : null);
        heroValidator.validateDuplicateDictionaryName(
                "Rarity",
                () -> rarityRepository.existsDuplicateLocalizedName(normalizedRu, normalizedEn, id)
        );
        imageReferenceValidator.validate(request.imageBucket(), request.imageObjectKey());
        rarity.setNameJson(request.nameJson());
        rarity.setStars(request.stars());
        rarity.setImageBucket(request.imageBucket());
        rarity.setImageObjectKey(request.imageObjectKey());

        return converter.toResponse(rarityRepository.save(rarity));
    }

    public void delete(Long id) {
        if (!rarityRepository.existsById(id)) {
            throw new NotFoundException("Rarity not found: " + id);
        }

        rarityRepository.deleteById(id);
    }

    private Rarity getEntityById(Long id) {
        return rarityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rarity not found: " + id));
    }
}

