package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.dto.request.ManaSpeedCreateRequest;
import com.gameplatform.mainservice.hero.dto.request.ManaSpeedUpdateRequest;
import com.gameplatform.mainservice.hero.dto.response.ManaSpeedResponse;
import com.gameplatform.mainservice.hero.mapper.ManaSpeedResponseConverter;
import com.gameplatform.mainservice.hero.service.ManaSpeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ManaSpeedFacade {

    private final ManaSpeedService manaSpeedService;
    private final ManaSpeedResponseConverter converter;

    public List<ManaSpeedResponse> getAll() {
        return converter.toResponseList(manaSpeedService.getAll());
    }

    public ManaSpeedResponse getById(Long id) {
        return converter.toResponse(manaSpeedService.getById(id));
    }

    public ManaSpeedResponse create(ManaSpeedCreateRequest request) {
        return converter.toResponse(manaSpeedService.create(request));
    }

    public ManaSpeedResponse update(Long id, ManaSpeedUpdateRequest request) {
        return converter.toResponse(manaSpeedService.update(id, request));
    }

    public void delete(Long id) {
        manaSpeedService.delete(id);
    }
}