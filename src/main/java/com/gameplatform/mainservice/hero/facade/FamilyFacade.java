package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.dto.request.FamilyUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.FamilyResponse;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.converter.FamilyResponseConverter;
import com.gameplatform.mainservice.hero.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FamilyFacade {

    private final FamilyService familyService;
    private final FamilyResponseConverter familyResponseConverter;

    public List<FamilyResponse> getAll() {
        return familyResponseConverter.toResponseList(familyService.getAll());
    }

    public CatalogPageResponse<FamilyResponse> getPage(int page, int size, String search) {
        return CatalogPageResponse.from(familyService.getPage(page, size, search).map(familyResponseConverter::toResponse));
    }

    public FamilyResponse getById(Long id) {
        return familyResponseConverter.toResponse(familyService.getById(id));
    }

    public FamilyResponse create(FamilyUpsertRequest request) {
        return familyResponseConverter.toResponse(familyService.create(request));
    }

    public FamilyResponse update(Long id, FamilyUpsertRequest request) {
        return familyResponseConverter.toResponse(familyService.update(id, request));
    }

    public void delete(Long id) {
        familyService.delete(id);
    }
}
