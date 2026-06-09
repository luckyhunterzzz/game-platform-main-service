package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.hero.converter.HeroClassEmblemBonusProfileResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.HeroClass;
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
        validateUniqueProfile(request, null);
        HeroClassEmblemBonusProfile entity = HeroClassEmblemBonusProfile.builder().build();
        applyUpsert(entity, request);

        return converter.toResponse(repository.save(entity));
    }

    public HeroClassEmblemBonusProfileResponse update(Long id, HeroClassEmblemBonusProfileUpsertRequest request) {
        HeroClassEmblemBonusProfile entity = getEntityById(id);
        validateUniqueProfile(request, id);
        applyUpsert(entity, request);

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

    private void validateUniqueProfile(HeroClassEmblemBonusProfileUpsertRequest request, Long id) {
        repository.findByHeroClassIdAndPathType(request.heroClassId(), request.pathType())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(p -> {
                    throw new BusinessValidationException("Profile already exists for heroClassId + pathType");
                });
    }

    private void applyUpsert(HeroClassEmblemBonusProfile entity, HeroClassEmblemBonusProfileUpsertRequest request) {
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
    }

    private List<HeroClassEmblemBonusProfile> sortProfiles(List<HeroClassEmblemBonusProfile> profiles) {
        Map<Long, String> heroClassNames = heroClassRepository.findAll().stream()
                .collect(Collectors.toMap(
                        HeroClass::getId,
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

