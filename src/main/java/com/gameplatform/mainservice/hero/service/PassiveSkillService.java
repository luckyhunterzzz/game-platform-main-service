package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.exception.exceptions.DictionaryItemInUseException;
import com.gameplatform.mainservice.hero.domain.entity.PassiveSkill;
import com.gameplatform.mainservice.hero.dto.response.HeroUsageReferenceResponse;
import com.gameplatform.mainservice.hero.repository.HeroPassiveSkillRepository;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import com.gameplatform.mainservice.hero.dto.request.PassiveSkillUpsertRequest;
import com.gameplatform.mainservice.hero.repository.PassiveSkillRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PassiveSkillService {

    private final PassiveSkillRepository repository;
    private final HeroPassiveSkillRepository heroPassiveSkillRepository;
    private final HeroRepository heroRepository;
    private final DictionaryCatalogSupport catalogSupport;

    public List<PassiveSkill> getAll() {
        return catalogSupport.sortLocalized(repository.findAll(), PassiveSkill::getNameJson);
    }

    public Page<PassiveSkill> getPage(int page, int size, String search) {
        return catalogSupport.pageLocalized(repository.findAll(), search, page, size, PassiveSkill::getNameJson);
    }

    public PassiveSkill getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PassiveSkill not found: " + id));
    }

    public PassiveSkill create(PassiveSkillUpsertRequest request) {
        PassiveSkill entity = PassiveSkill.builder()
                .nameJson(request.nameJson())
                .descriptionJson(request.descriptionJson())
                .build();

        return repository.save(entity);
    }

    public PassiveSkill update(Long id, PassiveSkillUpsertRequest request) {
        PassiveSkill entity = getById(id);

        entity.setNameJson(request.nameJson());
        entity.setDescriptionJson(request.descriptionJson());

        return repository.save(entity);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("PassiveSkill not found: " + id);
        }

        if (heroPassiveSkillRepository.existsByIdPassiveSkillId(id)) {
            throw new DictionaryItemInUseException(
                    "Passive skill is used by one or more heroes and cannot be deleted",
                    catalogSupport.sortLocalized(
                                    heroRepository.findAllByPassiveSkillId(id),
                                    hero -> hero.getNameJson()
                            ).stream()
                            .map(hero -> new HeroUsageReferenceResponse(
                                    hero.getId(),
                                    hero.getSlug(),
                                    hero.getNameJson(),
                                    hero.getStatus().name()
                            ))
                            .toList()
            );
        }

        repository.deleteById(id);
    }
}
