package com.gameplatform.mainservice.hero.controller;

import com.gameplatform.mainservice.hero.dto.request.HeroTagGroupUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroTagGroupResponse;
import com.gameplatform.mainservice.hero.service.HeroTagGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/heroes/role-groups")
@RequiredArgsConstructor
public class HeroTagGroupAdminController {

    private final HeroTagGroupService service;

    @GetMapping
    public ResponseEntity<List<HeroTagGroupResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/catalog")
    public ResponseEntity<CatalogPageResponse<HeroTagGroupResponse>> getCatalog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(service.getPage(page, size, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HeroTagGroupResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<HeroTagGroupResponse> create(@RequestBody @Valid HeroTagGroupUpsertRequest request) {
        HeroTagGroupResponse response = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HeroTagGroupResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid HeroTagGroupUpsertRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
