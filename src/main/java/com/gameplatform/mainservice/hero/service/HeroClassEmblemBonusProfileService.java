package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.domain.entity.HeroClassEmblemBonusProfile;
import com.gameplatform.mainservice.hero.domain.enums.EmblemPathType;
import com.gameplatform.mainservice.hero.dto.request.HeroClassEmblemBonusProfileCreateRequest;
import com.gameplatform.mainservice.hero.dto.request.HeroClassEmblemBonusProfileUpdateRequest;
import com.gameplatform.mainservice.hero.repository.HeroClassEmblemBonusProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HeroClassEmblemBonusProfileService {

    private final HeroClassEmblemBonusProfileRepository repository;

    public List<HeroClassEmblemBonusProfile> getAll() {
        return repository.findAll();
    }

    public HeroClassEmblemBonusProfile getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found: " + id));
    }

    public HeroClassEmblemBonusProfile create(HeroClassEmblemBonusProfileCreateRequest request) {

        repository.findByHeroClassIdAndPathType(request.heroClassId(), request.pathType())
                .ifPresent(p -> {
                    throw new IllegalStateException("Profile already exists for heroClassId + pathType");
                });

        HeroClassEmblemBonusProfile entity = HeroClassEmblemBonusProfile.builder()
                .heroClassId(request.heroClassId())
                .pathType(request.pathType())
                .attackBonus(request.attackBonus())
                .armorBonus(request.armorBonus())
                .hpBonus(request.hpBonus())
                .build();

        return repository.save(entity);
    }

    public HeroClassEmblemBonusProfile update(Long id, HeroClassEmblemBonusProfileUpdateRequest request) {
        HeroClassEmblemBonusProfile entity = getById(id);

        repository.findByHeroClassIdAndPathType(request.heroClassId(), request.pathType())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(p -> {
                    throw new IllegalStateException("Profile already exists for heroClassId + pathType");
                });

        entity.setHeroClassId(request.heroClassId());
        entity.setPathType(request.pathType());
        entity.setAttackBonus(request.attackBonus());
        entity.setArmorBonus(request.armorBonus());
        entity.setHpBonus(request.hpBonus());

        return repository.save(entity);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Profile not found: " + id);
        }
        repository.deleteById(id);
    }
}