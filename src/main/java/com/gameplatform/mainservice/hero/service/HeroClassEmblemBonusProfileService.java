package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.hero.converter.HeroClassEmblemBonusProfileResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.HeroClassEmblemBonusProfile;
import com.gameplatform.mainservice.hero.dto.request.HeroClassEmblemBonusProfileUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroClassEmblemBonusProfileResponse;
import com.gameplatform.mainservice.hero.repository.HeroClassRepository;
import com.gameplatform.mainservice.hero.repository.HeroClassEmblemBonusProfileRepository;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HeroClassEmblemBonusProfileService {

    private final HeroClassEmblemBonusProfileRepository repository;
    private final HeroClassRepository heroClassRepository;
    private final DictionaryCatalogSupport catalogSupport;
    private final HeroClassEmblemBonusProfileResponseConverter converter;

    public List<HeroClassEmblemBonusProfileResponse> getAll() {
        return converter.toResponseList(sortProfiles(repository.findAll()));
    }

    public CatalogPageResponse<HeroClassEmblemBonusProfileResponse> getPage(int page, int size, String search) {
        List<HeroClassEmblemBonusProfile> filtered = sortProfiles(repository.findAll()).stream()
                .filter(profile -> matchesProfile(profile, search))
                .toList();

        return CatalogPageResponse.from(catalogSupport.toPage(filtered, page, size).map(converter::toResponse));
    }

    public HeroClassEmblemBonusProfileResponse getById(Long id) {
        return converter.toResponse(getEntityById(id));
    }

    public HeroClassEmblemBonusProfileResponse create(HeroClassEmblemBonusProfileUpsertRequest request) {
        repository.findByHeroClassIdAndPathType(request.heroClassId(), request.pathType())
                .ifPresent(p -> {
                    throw new BusinessValidationException("Profile already exists for heroClassId + pathType");
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

        return converter.toResponse(repository.save(entity));
    }

    public HeroClassEmblemBonusProfileResponse update(Long id, HeroClassEmblemBonusProfileUpsertRequest request) {
        HeroClassEmblemBonusProfile entity = getEntityById(id);

        repository.findByHeroClassIdAndPathType(request.heroClassId(), request.pathType())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(p -> {
                    throw new BusinessValidationException("Profile already exists for heroClassId + pathType");
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

        return converter.toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Profile not found: " + id);
        }
        repository.deleteById(id);
    }

    private HeroClassEmblemBonusProfile getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Profile not found: " + id));
    }

    private List<HeroClassEmblemBonusProfile> sortProfiles(List<HeroClassEmblemBonusProfile> profiles) {
        Map<Long, String> heroClassNames = heroClassRepository.findAll().stream()
                .collect(Collectors.toMap(
                        item -> item.getId(),
                        item -> catalogSupport.sortableLocalized(item.getNameJson())
                ));

        return profiles.stream()
                .sorted(Comparator
                        .comparing((HeroClassEmblemBonusProfile item) -> heroClassNames.getOrDefault(item.getHeroClassId(), ""))
                        .thenComparing(item -> item.getPathType().name())
                        .thenComparing(HeroClassEmblemBonusProfile::getId))
                .toList();
    }

    private boolean matchesProfile(HeroClassEmblemBonusProfile profile, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }

        String normalizedSearch = catalogSupport.normalize(search);
        return heroClassRepository.findById(profile.getHeroClassId())
                .map(item -> catalogSupport.matchesLocalized(item.getNameJson(), normalizedSearch))
                .orElse(false)
                || catalogSupport.normalize(profile.getPathType().name()).contains(normalizedSearch);
    }
}


