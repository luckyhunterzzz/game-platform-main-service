package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.config.PublicCacheEvictionService;
import com.gameplatform.mainservice.hero.converter.ManaSpeedResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.ManaSpeed;
import com.gameplatform.mainservice.hero.dto.request.ManaSpeedUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.dto.response.ManaSpeedResponse;
import com.gameplatform.mainservice.hero.repository.ManaSpeedRepository;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManaSpeedService {

    private final ManaSpeedRepository manaSpeedRepository;
    private final DictionaryCatalogSupport catalogSupport;
    private final ManaSpeedResponseConverter converter;
    private final PublicCacheEvictionService publicCacheEvictionService;

    public List<ManaSpeedResponse> getAll() {
        return converter.toResponseList(catalogSupport.sortLocalized(manaSpeedRepository.findAll(), ManaSpeed::getNameJson));
    }

    public CatalogPageResponse<ManaSpeedResponse> getPage(int page, int size, String search) {
        return CatalogPageResponse.from(
                catalogSupport.pageLocalized(manaSpeedRepository.findAll(), search, page, size, ManaSpeed::getNameJson)
                        .map(converter::toResponse)
        );
    }

    public ManaSpeedResponse getById(Long id) {
        return converter.toResponse(getEntityById(id));
    }

    public ManaSpeedResponse create(ManaSpeedUpsertRequest request) {
        ManaSpeed entity = ManaSpeed.builder()
                .nameJson(request.nameJson())
                .descriptionJson(request.descriptionJson())
                .build();

        ManaSpeedResponse response = converter.toResponse(manaSpeedRepository.save(entity));
        publicCacheEvictionService.evictHeroCaches();
        return response;
    }

    public ManaSpeedResponse update(Long id, ManaSpeedUpsertRequest request) {
        ManaSpeed entity = getEntityById(id);

        entity.setNameJson(request.nameJson());
        entity.setDescriptionJson(request.descriptionJson());

        ManaSpeedResponse response = converter.toResponse(manaSpeedRepository.save(entity));
        publicCacheEvictionService.evictHeroCaches();
        return response;
    }

    public void delete(Long id) {
        if (!manaSpeedRepository.existsById(id)) {
            throw new NotFoundException("ManaSpeed not found: " + id);
        }
        manaSpeedRepository.deleteById(id);
        publicCacheEvictionService.evictHeroCaches();
    }

    private ManaSpeed getEntityById(Long id) {
        return manaSpeedRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ManaSpeed not found: " + id));
    }
}

