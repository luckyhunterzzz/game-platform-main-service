package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.dto.request.RarityCreateRequest;
import com.gameplatform.mainservice.hero.dto.request.RarityUpdateRequest;
import com.gameplatform.mainservice.hero.dto.response.RarityResponse;
import com.gameplatform.mainservice.hero.mapper.RarityResponseConverter;
import com.gameplatform.mainservice.hero.service.RarityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RarityFacade {

    private final RarityService rarityService;
    private final RarityResponseConverter rarityResponseConverter;

    public List<RarityResponse> getAll() {
        return rarityResponseConverter.toResponseList(rarityService.getAll());
    }

    public RarityResponse getById(Long id) {
        return rarityResponseConverter.toResponse(rarityService.getById(id));
    }

    public RarityResponse create(RarityCreateRequest request) {
        return rarityResponseConverter.toResponse(rarityService.create(request));
    }

    public RarityResponse update(Long id, RarityUpdateRequest request) {
        return rarityResponseConverter.toResponse(rarityService.update(id, request));
    }

    public void delete(Long id) {
        rarityService.delete(id);
    }
}