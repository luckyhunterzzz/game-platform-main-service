package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.domain.entity.ManaSpeed;
import com.gameplatform.mainservice.hero.dto.request.ManaSpeedUpsertRequest;
import com.gameplatform.mainservice.hero.repository.ManaSpeedRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManaSpeedService {

    private final ManaSpeedRepository manaSpeedRepository;

    public List<ManaSpeed> getAll() {
        return manaSpeedRepository.findAll();
    }

    public Page<ManaSpeed> getPage(int page, int size) {
        return manaSpeedRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
    }

    public ManaSpeed getById(Long id) {
        return manaSpeedRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ManaSpeed not found: " + id));
    }

    public ManaSpeed create(ManaSpeedUpsertRequest request) {
        ManaSpeed entity = ManaSpeed.builder()
                .nameJson(request.nameJson())
                .descriptionJson(request.descriptionJson())
                .build();

        return manaSpeedRepository.save(entity);
    }

    public ManaSpeed update(Long id, ManaSpeedUpsertRequest request) {
        ManaSpeed entity = getById(id);

        entity.setNameJson(request.nameJson());
        entity.setDescriptionJson(request.descriptionJson());

        return manaSpeedRepository.save(entity);
    }

    public void delete(Long id) {
        if (!manaSpeedRepository.existsById(id)) {
            throw new EntityNotFoundException("ManaSpeed not found: " + id);
        }
        manaSpeedRepository.deleteById(id);
    }
}
