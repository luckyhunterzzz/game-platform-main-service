package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.domain.entity.PassiveSkill;
import com.gameplatform.mainservice.hero.dto.request.PassiveSkillCreateRequest;
import com.gameplatform.mainservice.hero.dto.request.PassiveSkillUpdateRequest;
import com.gameplatform.mainservice.hero.repository.PassiveSkillRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PassiveSkillService {

    private final PassiveSkillRepository repository;

    public List<PassiveSkill> getAll() {
        return repository.findAll();
    }

    public PassiveSkill getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PassiveSkill not found: " + id));
    }

    public PassiveSkill create(PassiveSkillCreateRequest request) {
        PassiveSkill entity = PassiveSkill.builder()
                .nameJson(request.nameJson())
                .descriptionJson(request.descriptionJson())
                .build();

        return repository.save(entity);
    }

    public PassiveSkill update(Long id, PassiveSkillUpdateRequest request) {
        PassiveSkill entity = getById(id);

        entity.setNameJson(request.nameJson());
        entity.setDescriptionJson(request.descriptionJson());

        return repository.save(entity);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("PassiveSkill not found: " + id);
        }
        repository.deleteById(id);
    }
}