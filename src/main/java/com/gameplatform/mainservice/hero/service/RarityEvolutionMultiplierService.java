package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.converter.RarityEvolutionMultiplierResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.RarityEvolutionMultiplier;
import com.gameplatform.mainservice.hero.domain.enums.EvolutionStageCode;
import com.gameplatform.mainservice.hero.dto.request.RarityEvolutionMultiplierUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.dto.response.RarityEvolutionMultiplierResponse;
import com.gameplatform.mainservice.hero.repository.RarityRepository;
import com.gameplatform.mainservice.hero.repository.RarityEvolutionMultiplierRepository;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RarityEvolutionMultiplierService {

    private final RarityEvolutionMultiplierRepository repository;
    private final RarityRepository rarityRepository;
    private final DictionaryCatalogSupport catalogSupport;
    private final RarityEvolutionMultiplierResponseConverter converter;

    public List<RarityEvolutionMultiplierResponse> getAll() {
        return converter.toResponseList(sortMultipliers(repository.findAll()));
    }

    public CatalogPageResponse<RarityEvolutionMultiplierResponse> getPage(int page, int size, String search) {
        List<RarityEvolutionMultiplier> filtered = sortMultipliers(repository.findAll()).stream()
                .filter(item -> matchesMultiplier(item, search))
                .toList();

        return CatalogPageResponse.from(catalogSupport.toPage(filtered, page, size).map(converter::toResponse));
    }

    public RarityEvolutionMultiplierResponse getById(Long id) {
        return converter.toResponse(getEntityById(id));
    }

    public RarityEvolutionMultiplierResponse create(RarityEvolutionMultiplierUpsertRequest request) {
        RarityEvolutionMultiplier entity = RarityEvolutionMultiplier.builder()
                .rarityId(request.rarityId())
                .stageCode(request.stageCode())
                .attackMultiplier(request.attackMultiplier())
                .armorMultiplier(request.armorMultiplier())
                .hpMultiplier(request.hpMultiplier())
                .build();

        return converter.toResponse(repository.save(entity));
    }

    public RarityEvolutionMultiplierResponse update(Long id, RarityEvolutionMultiplierUpsertRequest request) {
        RarityEvolutionMultiplier entity = getEntityById(id);

        entity.setRarityId(request.rarityId());
        entity.setStageCode(request.stageCode());
        entity.setAttackMultiplier(request.attackMultiplier());
        entity.setArmorMultiplier(request.armorMultiplier());
        entity.setHpMultiplier(request.hpMultiplier());

        return converter.toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("RarityEvolutionMultiplier not found: " + id);
        }
        repository.deleteById(id);
    }

    private RarityEvolutionMultiplier getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("RarityEvolutionMultiplier not found: " + id));
    }

    private List<RarityEvolutionMultiplier> sortMultipliers(List<RarityEvolutionMultiplier> multipliers) {
        Map<Long, String> rarityNames = rarityRepository.findAll().stream()
                .collect(Collectors.toMap(
                        item -> item.getId(),
                        item -> catalogSupport.sortableLocalized(item.getNameJson())
                ));

        return multipliers.stream()
                .sorted(Comparator
                        .comparing((RarityEvolutionMultiplier item) -> rarityNames.getOrDefault(item.getRarityId(), ""))
                        .thenComparingInt(item -> stageOrder(item.getStageCode()))
                        .thenComparing(RarityEvolutionMultiplier::getId))
                .toList();
    }

    private boolean matchesMultiplier(RarityEvolutionMultiplier item, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }

        String normalizedSearch = catalogSupport.normalize(search);
        return rarityRepository.findById(item.getRarityId())
                .map(rarity -> catalogSupport.matchesLocalized(rarity.getNameJson(), normalizedSearch))
                .orElse(false)
                || catalogSupport.normalize(item.getStageCode().name()).contains(normalizedSearch)
                || catalogSupport.normalize(stageLabel(item.getStageCode())).contains(normalizedSearch);
    }

    private int stageOrder(EvolutionStageCode stageCode) {
        return switch (stageCode) {
            case ASCENSION_4_80 -> 0;
            case ASCENSION_4_85 -> 1;
            case ASCENSION_4_90 -> 2;
        };
    }

    private String stageLabel(EvolutionStageCode stageCode) {
        return switch (stageCode) {
            case ASCENSION_4_80 -> "4.80";
            case ASCENSION_4_85 -> "4.85";
            case ASCENSION_4_90 -> "4.90";
        };
    }
}

