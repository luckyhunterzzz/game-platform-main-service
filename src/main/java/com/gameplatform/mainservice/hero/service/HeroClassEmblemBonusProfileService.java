package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.domain.entity.HeroClassEmblemBonusProfile;
import com.gameplatform.mainservice.hero.dto.request.HeroClassEmblemBonusProfileUpsertRequest;
import com.gameplatform.mainservice.hero.repository.HeroClassEmblemBonusProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HeroClassEmblemBonusProfileService {

    private final HeroClassEmblemBonusProfileRepository repository;

    public List<HeroClassEmblemBonusProfile> getAll() {
        return repository.findAll();
    }

    public Page<HeroClassEmblemBonusProfile> getPage(int page, int size) {
        return repository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
    }

    public HeroClassEmblemBonusProfile getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found: " + id));
    }

    public HeroClassEmblemBonusProfile create(HeroClassEmblemBonusProfileUpsertRequest request) {

        repository.findByHeroClassIdAndPathType(request.heroClassId(), request.pathType())
                .ifPresent(p -> {
                    throw new IllegalStateException("Profile already exists for heroClassId + pathType");
                });

        HeroClassEmblemBonusProfile entity = HeroClassEmblemBonusProfile.builder()
                .heroClassId(request.heroClassId())
                .pathType(request.pathType())
                .attackFlatBonus(request.attackFlatBonus())
                .armorFlatBonus(request.armorFlatBonus())
                .hpFlatBonus(request.hpFlatBonus())
                .attackPercentBonus(request.attackPercentBonus())
                .armorPercentBonus(request.armorPercentBonus())
                .hpPercentBonus(request.hpPercentBonus())
                .masterAttackBonus(request.masterAttackBonus())
                .masterArmorBonus(request.masterArmorBonus())
                .masterHpBonus(request.masterHpBonus())
                .build();

        return repository.save(entity);
    }

    public HeroClassEmblemBonusProfile update(Long id, HeroClassEmblemBonusProfileUpsertRequest request) {
        HeroClassEmblemBonusProfile entity = getById(id);

        repository.findByHeroClassIdAndPathType(request.heroClassId(), request.pathType())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(p -> {
                    throw new IllegalStateException("Profile already exists for heroClassId + pathType");
                });

        entity.setHeroClassId(request.heroClassId());
        entity.setPathType(request.pathType());
        entity.setAttackFlatBonus(request.attackFlatBonus());
        entity.setArmorFlatBonus(request.armorFlatBonus());
        entity.setHpFlatBonus(request.hpFlatBonus());
        entity.setAttackPercentBonus(request.attackPercentBonus());
        entity.setArmorPercentBonus(request.armorPercentBonus());
        entity.setHpPercentBonus(request.hpPercentBonus());
        entity.setMasterAttackBonus(request.masterAttackBonus());
        entity.setMasterArmorBonus(request.masterArmorBonus());
        entity.setMasterHpBonus(request.masterHpBonus());

        return repository.save(entity);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Profile not found: " + id);
        }
        repository.deleteById(id);
    }
}
