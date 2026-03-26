package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.dto.request.FamilyCreateRequest;
import com.gameplatform.mainservice.hero.dto.request.FamilyUpdateRequest;
import com.gameplatform.mainservice.hero.dto.response.FamilyResponse;
import com.gameplatform.mainservice.hero.mapper.FamilyResponseConverter;
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

    public FamilyResponse getById(Long id) {
        return familyResponseConverter.toResponse(familyService.getById(id));
    }

    public FamilyResponse create(FamilyCreateRequest request) {
        return familyResponseConverter.toResponse(familyService.create(request));
    }

    public FamilyResponse update(Long id, FamilyUpdateRequest request) {
        return familyResponseConverter.toResponse(familyService.update(id, request));
    }

    public void delete(Long id) {
        familyService.delete(id);
    }
}