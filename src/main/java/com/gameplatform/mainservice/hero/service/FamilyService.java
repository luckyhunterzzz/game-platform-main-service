package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.domain.entity.Family;
import com.gameplatform.mainservice.hero.dto.request.FamilyUpsertRequest;
import com.gameplatform.mainservice.hero.repository.FamilyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final DictionaryCatalogSupport catalogSupport;

    public List<Family> getAll() {
        return catalogSupport.sortLocalized(familyRepository.findAll(), Family::getNameJson);
    }

    public Page<Family> getPage(int page, int size, String search) {
        return catalogSupport.pageLocalized(familyRepository.findAll(), search, page, size, Family::getNameJson);
    }

    public Family getById(Long id) {
        return familyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Family not found: " + id));
    }

    public Family create(FamilyUpsertRequest request) {
        Family family = Family.builder()
                .nameJson(request.nameJson())
                .descriptionJson(request.descriptionJson())
                .build();

        return familyRepository.save(family);
    }

    public Family update(Long id, FamilyUpsertRequest request) {
        Family family = getById(id);
        family.setNameJson(request.nameJson());
        family.setDescriptionJson(request.descriptionJson());

        return familyRepository.save(family);
    }

    public void delete(Long id) {
        if (!familyRepository.existsById(id)) {
            throw new EntityNotFoundException("Family not found: " + id);
        }
        familyRepository.deleteById(id);
    }
}
