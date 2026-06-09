package com.gameplatform.mainservice.hero.controller;

import com.gameplatform.mainservice.hero.dto.request.ManaSpeedUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.dto.response.ManaSpeedResponse;
import com.gameplatform.mainservice.hero.service.ManaSpeedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/heroes/mana-speeds")
@RequiredArgsConstructor
public class ManaSpeedAdminController {

    private final ManaSpeedService service;

    @GetMapping
    public ResponseEntity<List<ManaSpeedResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/catalog")
    public ResponseEntity<CatalogPageResponse<ManaSpeedResponse>> getCatalog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(service.getPage(page, size, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManaSpeedResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<ManaSpeedResponse> create(@RequestBody @Valid ManaSpeedUpsertRequest request) {
        ManaSpeedResponse response = service.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ManaSpeedResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid ManaSpeedUpsertRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
