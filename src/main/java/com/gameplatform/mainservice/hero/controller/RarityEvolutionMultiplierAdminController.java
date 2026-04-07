package com.gameplatform.mainservice.hero.controller;

import com.gameplatform.mainservice.hero.dto.request.RarityEvolutionMultiplierUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.dto.response.RarityEvolutionMultiplierResponse;
import com.gameplatform.mainservice.hero.facade.RarityEvolutionMultiplierFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/heroes/rarity-evolution-multipliers")
@RequiredArgsConstructor
public class RarityEvolutionMultiplierAdminController {

    private final RarityEvolutionMultiplierFacade facade;

    @GetMapping
    public ResponseEntity<List<RarityEvolutionMultiplierResponse>> getAll() {
        return ResponseEntity.ok(facade.getAll());
    }

    @GetMapping("/catalog")
    public ResponseEntity<CatalogPageResponse<RarityEvolutionMultiplierResponse>> getCatalog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(facade.getPage(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RarityEvolutionMultiplierResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(facade.getById(id));
    }

    @PostMapping
    public ResponseEntity<RarityEvolutionMultiplierResponse> create(
            @RequestBody @Valid RarityEvolutionMultiplierUpsertRequest request
    ) {
        RarityEvolutionMultiplierResponse response = facade.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RarityEvolutionMultiplierResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid RarityEvolutionMultiplierUpsertRequest request
    ) {
        return ResponseEntity.ok(facade.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        facade.delete(id);
        return ResponseEntity.noContent().build();
    }
}
