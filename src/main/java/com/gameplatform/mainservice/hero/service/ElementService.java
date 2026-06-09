package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.converter.ElementResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.Element;
import com.gameplatform.mainservice.hero.dto.request.ElementUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.dto.response.ElementResponse;
import com.gameplatform.mainservice.hero.repository.ElementRepository;
import com.gameplatform.mainservice.hero.validation.HeroValidator;
import com.gameplatform.mainservice.media.validation.ImageReferenceValidator;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ElementService {

    private final ElementRepository elementRepository;
    private final DictionaryCatalogSupport catalogSupport;
    private final HeroValidator heroValidator;
    private final ImageReferenceValidator imageReferenceValidator;
    private final ElementResponseConverter converter;

    public List<ElementResponse> getAll() {
        return converter.toResponseList(catalogSupport.sortLocalized(elementRepository.findAll(), Element::getNameJson));
    }

    public CatalogPageResponse<ElementResponse> getPage(int page, int size, String search) {
        return CatalogPageResponse.from(
                catalogSupport.pageLocalized(elementRepository.findAll(), search, page, size, Element::getNameJson)
                        .map(converter::toResponse)
        );
    }

    public ElementResponse getById(Long id) {
        return converter.toResponse(getEntityById(id));
    }

    public ElementResponse create(ElementUpsertRequest request) {
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

        return converter.toResponse(elementRepository.save(element));
    }

    public ElementResponse update(Long id, ElementUpsertRequest request) {
        Element element = getEntityById(id);
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

        return converter.toResponse(elementRepository.save(element));
    }

    public void delete(Long id) {
        if (!elementRepository.existsById(id)) {
            throw new NotFoundException("Element not found: " + id);
        }
        elementRepository.deleteById(id);
    }

    private Element getEntityById(Long id) {
        return elementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Element not found: " + id));
    }
}

