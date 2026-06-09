package com.gameplatform.mainservice.hero.controller;

import com.gameplatform.mainservice.hero.dto.request.HeroClassEmblemBonusProfileUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroClassEmblemBonusProfileResponse;
import com.gameplatform.mainservice.hero.service.HeroClassEmblemBonusProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/heroes/emblem-profiles")
@RequiredArgsConstructor
public class HeroClassEmblemBonusProfileAdminController {

    private final HeroClassEmblemBonusProfileService service;

    @GetMapping
    public ResponseEntity<List<HeroClassEmblemBonusProfileResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/catalog")
    public ResponseEntity<CatalogPageResponse<HeroClassEmblemBonusProfileResponse>> getCatalog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(service.getPage(page, size, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HeroClassEmblemBonusProfileResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<HeroClassEmblemBonusProfileResponse> create(
            @RequestBody @Valid HeroClassEmblemBonusProfileUpsertRequest request
    ) {
        HeroClassEmblemBonusProfileResponse response = service.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HeroClassEmblemBonusProfileResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid HeroClassEmblemBonusProfileUpsertRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
