package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.domain.entity.HeroClass;
import com.gameplatform.mainservice.hero.dto.request.HeroClassCreateRequest;
import com.gameplatform.mainservice.hero.dto.request.HeroClassUpdateRequest;
import com.gameplatform.mainservice.hero.repository.HeroClassRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HeroClassService {

    private final HeroClassRepository repository;

    public List<HeroClass> getAll() {
        return repository.findAll();
    }

    public HeroClass getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("HeroClass not found: " + id));
    }

    public HeroClass create(HeroClassCreateRequest request) {
        HeroClass entity = HeroClass.builder()
                .nameJson(request.nameJson())
                .baseNameJson(request.baseNameJson())
                .baseDescriptionJson(request.baseDescriptionJson())
                .masterNameJson(request.masterNameJson())
                .masterDescriptionJson(request.masterDescriptionJson())
                .build();

        return repository.save(entity);
    }

    public HeroClass update(Long id, HeroClassUpdateRequest request) {
        HeroClass entity = getById(id);

        entity.setNameJson(request.nameJson());
        entity.setBaseNameJson(request.baseNameJson());
        entity.setBaseDescriptionJson(request.baseDescriptionJson());
        entity.setMasterNameJson(request.masterNameJson());
        entity.setMasterDescriptionJson(request.masterDescriptionJson());

        return repository.save(entity);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("HeroClass not found: " + id);
        }
        repository.deleteById(id);
    }
}