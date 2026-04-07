package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.dto.request.PassiveSkillUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.dto.response.PassiveSkillResponse;
import com.gameplatform.mainservice.hero.converter.PassiveSkillResponseConverter;
import com.gameplatform.mainservice.hero.service.PassiveSkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PassiveSkillFacade {

    private final PassiveSkillService service;
    private final PassiveSkillResponseConverter converter;

    public List<PassiveSkillResponse> getAll() {
        return converter.toResponseList(service.getAll());
    }

    public CatalogPageResponse<PassiveSkillResponse> getPage(int page, int size, String search) {
        return CatalogPageResponse.from(service.getPage(page, size, search).map(converter::toResponse));
    }

    public PassiveSkillResponse getById(Long id) {
        return converter.toResponse(service.getById(id));
    }

    public PassiveSkillResponse create(PassiveSkillUpsertRequest request) {
        return converter.toResponse(service.create(request));
    }

    public PassiveSkillResponse update(Long id, PassiveSkillUpsertRequest request) {
        return converter.toResponse(service.update(id, request));
    }

    public void delete(Long id) {
        service.delete(id);
    }
}
