package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.domain.entity.RarityEvolutionMultiplier;
import com.gameplatform.mainservice.hero.dto.request.RarityEvolutionMultiplierCreateRequest;
import com.gameplatform.mainservice.hero.dto.request.RarityEvolutionMultiplierUpdateRequest;
import com.gameplatform.mainservice.hero.repository.RarityEvolutionMultiplierRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RarityEvolutionMultiplierService {

    private final RarityEvolutionMultiplierRepository repository;

    public List<RarityEvolutionMultiplier> getAll() {
        return repository.findAll();
    }

    public RarityEvolutionMultiplier getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("RarityEvolutionMultiplier not found: " + id));
    }

    public RarityEvolutionMultiplier create(RarityEvolutionMultiplierCreateRequest request) {
        RarityEvolutionMultiplier entity = RarityEvolutionMultiplier.builder()
                .rarityId(request.rarityId())
                .stageCode(request.stageCode())
                .attackMultiplier(request.attackMultiplier())
                .armorMultiplier(request.armorMultiplier())
                .hpMultiplier(request.hpMultiplier())
                .build();

        return repository.save(entity);
    }

    public RarityEvolutionMultiplier update(Long id, RarityEvolutionMultiplierUpdateRequest request) {
        RarityEvolutionMultiplier entity = getById(id);

        entity.setRarityId(request.rarityId());
        entity.setStageCode(request.stageCode());
        entity.setAttackMultiplier(request.attackMultiplier());
        entity.setArmorMultiplier(request.armorMultiplier());
        entity.setHpMultiplier(request.hpMultiplier());

        return repository.save(entity);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("RarityEvolutionMultiplier not found: " + id);
        }
        repository.deleteById(id);
    }
}