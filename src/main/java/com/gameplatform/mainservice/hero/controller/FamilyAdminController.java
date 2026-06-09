package com.gameplatform.mainservice.hero.controller;

import com.gameplatform.mainservice.hero.dto.request.FamilyUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.dto.response.FamilyResponse;
import com.gameplatform.mainservice.hero.service.FamilyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/heroes/families")
@RequiredArgsConstructor
public class FamilyAdminController {

    private final FamilyService service;

    @GetMapping
    public ResponseEntity<List<FamilyResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/catalog")
    public ResponseEntity<CatalogPageResponse<FamilyResponse>> getCatalog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(service.getPage(page, size, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FamilyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<FamilyResponse> create(@RequestBody @Valid FamilyUpsertRequest request) {
        FamilyResponse response = service.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FamilyResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid FamilyUpsertRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
