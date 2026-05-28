package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.domain.entity.Element;
import com.gameplatform.mainservice.hero.dto.request.ElementUpsertRequest;
import com.gameplatform.mainservice.hero.repository.ElementRepository;
import com.gameplatform.mainservice.hero.validation.HeroValidator;
import com.gameplatform.mainservice.media.validation.ImageReferenceValidator;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ElementService {

    private final ElementRepository elementRepository;
    private final DictionaryCatalogSupport catalogSupport;
    private final HeroValidator heroValidator;
    private final ImageReferenceValidator imageReferenceValidator;

    public List<Element> getAll() {
        return catalogSupport.sortLocalized(elementRepository.findAll(), Element::getNameJson);
    }

    public Page<Element> getPage(int page, int size, String search) {
        return catalogSupport.pageLocalized(elementRepository.findAll(), search, page, size, Element::getNameJson);
    }

    public Element getById(Long id) {
        return elementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Element not found: " + id));
    }

    public Element create(ElementUpsertRequest request) {
        String normalizedRu = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().ru() : null);
        String normalizedEn = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().en() : null);
        heroValidator.validateDuplicateDictionaryName(
                "Element",
                () -> elementRepository.existsDuplicateLocalizedName(normalizedRu, normalizedEn, null)
        );
        imageReferenceValidator.validate(request.imageBucket(), request.imageObjectKey());

        Element element = new Element();
        element.setNameJson(request.nameJson());
        element.setImageBucket(request.imageBucket());
        element.setImageObjectKey(request.imageObjectKey());

        return elementRepository.save(element);
    }

    public Element update(Long id, ElementUpsertRequest request) {
        Element element = getById(id);
        String normalizedRu = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().ru() : null);
        String normalizedEn = heroValidator.normalizeDictionaryName(request.nameJson() != null ? request.nameJson().en() : null);
        heroValidator.validateDuplicateDictionaryName(
                "Element",
                () -> elementRepository.existsDuplicateLocalizedName(normalizedRu, normalizedEn, id)
        );
        imageReferenceValidator.validate(request.imageBucket(), request.imageObjectKey());

        element.setNameJson(request.nameJson());
        element.setImageBucket(request.imageBucket());
        element.setImageObjectKey(request.imageObjectKey());

        return elementRepository.save(element);
    }

    public void delete(Long id) {
        if (!elementRepository.existsById(id)) {
            throw new NotFoundException("Element not found: " + id);
        }
        elementRepository.deleteById(id);
    }
}

