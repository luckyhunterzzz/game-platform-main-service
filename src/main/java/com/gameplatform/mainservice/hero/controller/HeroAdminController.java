package com.gameplatform.mainservice.hero.controller;

import com.gameplatform.mainservice.hero.dto.request.HeroUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroResponse;
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

    @GetMapping("/{id}")
    public ResponseEntity<HeroResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(heroFacade.getById(id));
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
