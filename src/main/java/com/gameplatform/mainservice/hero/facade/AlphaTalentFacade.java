package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.dto.request.AlphaTalentUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.AlphaTalentResponse;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.converter.AlphaTalentResponseConverter;
import com.gameplatform.mainservice.hero.service.AlphaTalentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AlphaTalentFacade {

    private final AlphaTalentService service;
    private final AlphaTalentResponseConverter converter;

    public List<AlphaTalentResponse> getAll() {
        return converter.toResponseList(service.getAll());
    }

    public CatalogPageResponse<AlphaTalentResponse> getPage(int page, int size, String search) {
        return CatalogPageResponse.from(service.getPage(page, size, search).map(converter::toResponse));
    }

    public AlphaTalentResponse getById(Long id) {
        return converter.toResponse(service.getById(id));
    }

    public AlphaTalentResponse create(AlphaTalentUpsertRequest request) {
        return converter.toResponse(service.create(request));
    }

    public AlphaTalentResponse update(Long id, AlphaTalentUpsertRequest request) {
        return converter.toResponse(service.update(id, request));
    }

    public void delete(Long id) {
        service.delete(id);
    }
}
