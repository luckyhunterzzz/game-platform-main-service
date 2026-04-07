package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.dto.request.ElementUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.ElementResponse;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.converter.ElementResponseConverter;
import com.gameplatform.mainservice.hero.service.ElementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ElementFacade {

    private final ElementService elementService;
    private final ElementResponseConverter converter;

    public List<ElementResponse> getAll() {
        return converter.toResponseList(elementService.getAll());
    }

    public CatalogPageResponse<ElementResponse> getPage(int page, int size, String search) {
        return CatalogPageResponse.from(elementService.getPage(page, size, search).map(converter::toResponse));
    }

    public ElementResponse getById(Long id) {
        return converter.toResponse(elementService.getById(id));
    }

    public ElementResponse create(ElementUpsertRequest request) {
        return converter.toResponse(elementService.create(request));
    }

    public ElementResponse update(Long id, ElementUpsertRequest request) {
        return converter.toResponse(elementService.update(id, request));
    }

    public void delete(Long id) {
        elementService.delete(id);
    }
}
