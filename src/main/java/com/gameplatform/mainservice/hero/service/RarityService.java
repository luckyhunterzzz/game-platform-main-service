package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.domain.entity.Rarity;
import com.gameplatform.mainservice.hero.dto.request.RarityUpsertRequest;
import com.gameplatform.mainservice.hero.repository.RarityRepository;
import com.gameplatform.mainservice.hero.validation.HeroValidator;
import com.gameplatform.mainservice.media.validation.ImageReferenceValidator;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RarityService {

    private final RarityRepository rarityRepository;
    private final DictionaryCatalogSupport catalogSupport;
    private final HeroValidator heroValidator;
    private final ImageReferenceValidator imageReferenceValidator;

    public List<Rarity> getAll() {
        return catalogSupport.sortLocalized(rarityRepository.findAll(), Rarity::getNameJson);
    }

    public Page<Rarity> getPage(int page, int size, String search) {
        return catalogSupport.pageLocalized(rarityRepository.findAll(), search, page, size, Rarity::getNameJson);
    }

    public Rarity getById(Long id) {
        return rarityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rarity not found: " + id));
    }

    public Rarity create(RarityUpsertRequest request) {
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

        return rarityRepository.save(rarity);
    }

    public Rarity update(Long id, RarityUpsertRequest request) {
        Rarity rarity = getById(id);
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

        return rarityRepository.save(rarity);
    }

    public void delete(Long id) {
        if (!rarityRepository.existsById(id)) {
            throw new NotFoundException("Rarity not found: " + id);
        }

        rarityRepository.deleteById(id);
    }
}

