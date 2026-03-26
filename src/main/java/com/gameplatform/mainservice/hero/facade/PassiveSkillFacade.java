package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.dto.request.PassiveSkillCreateRequest;
import com.gameplatform.mainservice.hero.dto.request.PassiveSkillUpdateRequest;
import com.gameplatform.mainservice.hero.dto.response.PassiveSkillResponse;
import com.gameplatform.mainservice.hero.mapper.PassiveSkillResponseConverter;
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

    public PassiveSkillResponse getById(Long id) {
        return converter.toResponse(service.getById(id));
    }

    public PassiveSkillResponse create(PassiveSkillCreateRequest request) {
        return converter.toResponse(service.create(request));
    }

    public PassiveSkillResponse update(Long id, PassiveSkillUpdateRequest request) {
        return converter.toResponse(service.update(id, request));
    }

    public void delete(Long id) {
        service.delete(id);
    }
}