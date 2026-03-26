package com.gameplatform.mainservice.hero.controller;

import com.gameplatform.mainservice.hero.dto.request.HeroClassCreateRequest;
import com.gameplatform.mainservice.hero.dto.request.HeroClassUpdateRequest;
import com.gameplatform.mainservice.hero.dto.response.FamilyResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroClassResponse;
import com.gameplatform.mainservice.hero.facade.HeroClassFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/heroes/hero-classes")
@RequiredArgsConstructor
public class HeroClassAdminController {

    private final HeroClassFacade facade;

    @GetMapping
    public ResponseEntity<List<HeroClassResponse>> getAll() {
        return ResponseEntity.ok(facade.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HeroClassResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(facade.getById(id));
    }

    @PostMapping
    public ResponseEntity<HeroClassResponse> create(@RequestBody @Valid HeroClassCreateRequest request) {
        HeroClassResponse response = facade.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HeroClassResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid HeroClassUpdateRequest request
    ) {
        return ResponseEntity.ok(facade.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        facade.delete(id);
        return ResponseEntity.noContent().build();
    }
}