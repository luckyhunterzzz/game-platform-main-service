package com.gameplatform.mainservice.hero.controller;

import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.dto.request.HeroStatCalculationRequest;
import com.gameplatform.mainservice.hero.dto.request.HeroUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroAdminVariantsResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroAdminPageResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroNextCostumeIndexResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroStatCalculationResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroSlugAvailabilityResponse;
import com.gameplatform.mainservice.hero.facade.HeroFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/heroes")
@RequiredArgsConstructor
public class HeroAdminController {

    private final HeroFacade heroFacade;

    @GetMapping
    public ResponseEntity<List<HeroResponse>> getAll() {
        return ResponseEntity.ok(heroFacade.getAll());
    }

    @GetMapping("/catalog")
    public ResponseEntity<HeroAdminPageResponse> getCatalog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Long> rarityIds,
            @RequestParam(required = false) List<com.gameplatform.mainservice.hero.domain.enums.HeroStatus> statuses
    ) {
        return ResponseEntity.ok(heroFacade.getCatalog(page, size, search, rarityIds, statuses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HeroResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(heroFacade.getById(id));
    }

    @GetMapping("/{id}/variants")
    public ResponseEntity<HeroAdminVariantsResponse> getVariants(
            @PathVariable Long id,
            @RequestParam(defaultValue = "RU") HeroLanguage language
    ) {
        return ResponseEntity.ok(heroFacade.getVariants(id, language));
    }

    @GetMapping("/slug-availability")
    public ResponseEntity<HeroSlugAvailabilityResponse> getSlugAvailability(
            @RequestParam String slug,
            @RequestParam(required = false) Long excludeId
    ) {
        return ResponseEntity.ok(heroFacade.getSlugAvailability(slug, excludeId));
    }

    @GetMapping("/next-costume-index")
    public ResponseEntity<HeroNextCostumeIndexResponse> getNextCostumeIndex(
            @RequestParam Long baseHeroId
    ) {
        return ResponseEntity.ok(heroFacade.getNextCostumeIndex(baseHeroId));
    }

    @PostMapping("/{id}/stats/calculate")
    public ResponseEntity<HeroStatCalculationResponse> calculateStats(
            @PathVariable Long id,
            @RequestBody @Valid HeroStatCalculationRequest request
    ) {
        return ResponseEntity.ok(heroFacade.calculateStats(id, request));
    }

    @PostMapping
    public ResponseEntity<HeroResponse> create(@RequestBody @Valid HeroUpsertRequest request) {
        HeroResponse response = heroFacade.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HeroResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid HeroUpsertRequest request
    ) {
        return ResponseEntity.ok(heroFacade.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        heroFacade.delete(id);
        return ResponseEntity.noContent().build();
    }
}
