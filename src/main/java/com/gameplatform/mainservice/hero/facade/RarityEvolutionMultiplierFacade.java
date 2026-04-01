package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.dto.request.RarityEvolutionMultiplierUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.RarityEvolutionMultiplierResponse;
import com.gameplatform.mainservice.hero.converter.RarityEvolutionMultiplierResponseConverter;
import com.gameplatform.mainservice.hero.service.RarityEvolutionMultiplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RarityEvolutionMultiplierFacade {

    private final RarityEvolutionMultiplierService service;
    private final RarityEvolutionMultiplierResponseConverter converter;

    public List<RarityEvolutionMultiplierResponse> getAll() {
        return converter.toResponseList(service.getAll());
    }

    public RarityEvolutionMultiplierResponse getById(Long id) {
        return converter.toResponse(service.getById(id));
    }

    public RarityEvolutionMultiplierResponse create(RarityEvolutionMultiplierUpsertRequest request) {
        return converter.toResponse(service.create(request));
    }

    public RarityEvolutionMultiplierResponse update(Long id, RarityEvolutionMultiplierUpsertRequest request) {
        return converter.toResponse(service.update(id, request));
    }

    public void delete(Long id) {
        service.delete(id);
    }
}
