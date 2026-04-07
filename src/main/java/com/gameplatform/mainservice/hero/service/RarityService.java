package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.domain.entity.Rarity;
import com.gameplatform.mainservice.hero.dto.request.RarityUpsertRequest;
import com.gameplatform.mainservice.hero.repository.RarityRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RarityService {

    private final RarityRepository rarityRepository;

    public List<Rarity> getAll() {
        return rarityRepository.findAll();
    }

    public Page<Rarity> getPage(int page, int size) {
        return rarityRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
    }

    public Rarity getById(Long id) {
        return rarityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rarity not found: " + id));
    }

    public Rarity create(RarityUpsertRequest request) {
        Rarity rarity = Rarity.builder()
                .nameJson(request.nameJson())
                .stars(request.stars())
                .build();

        return rarityRepository.save(rarity);
    }

    public Rarity update(Long id, RarityUpsertRequest request) {
        Rarity rarity = getById(id);
        rarity.setNameJson(request.nameJson());
        rarity.setStars(request.stars());

        return rarityRepository.save(rarity);
    }

    public void delete(Long id) {
        if (!rarityRepository.existsById(id)) {
            throw new EntityNotFoundException("Rarity not found: " + id);
        }

        rarityRepository.deleteById(id);
    }
}
