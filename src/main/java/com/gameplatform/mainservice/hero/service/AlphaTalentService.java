package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.domain.entity.AlphaTalent;
import com.gameplatform.mainservice.hero.dto.request.AlphaTalentUpsertRequest;
import com.gameplatform.mainservice.hero.repository.AlphaTalentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlphaTalentService {

    private final AlphaTalentRepository repository;

    public List<AlphaTalent> getAll() {
        return repository.findAll();
    }

    public Page<AlphaTalent> getPage(int page, int size) {
        return repository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
    }

    public AlphaTalent getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("AlphaTalent not found: " + id));
    }

    public AlphaTalent create(AlphaTalentUpsertRequest request) {
        AlphaTalent entity = AlphaTalent.builder()
                .nameJson(request.nameJson())
                .descriptionJson(request.descriptionJson())
                .build();

        return repository.save(entity);
    }

    public AlphaTalent update(Long id, AlphaTalentUpsertRequest request) {
        AlphaTalent entity = getById(id);

        entity.setNameJson(request.nameJson());
        entity.setDescriptionJson(request.descriptionJson());

        return repository.save(entity);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("AlphaTalent not found: " + id);
        }
        repository.deleteById(id);
    }
}
