package com.gameplatform.mainservice.hero.controller;

import com.gameplatform.mainservice.hero.dto.request.ElementUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.ElementResponse;
import com.gameplatform.mainservice.hero.facade.ElementFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/heroes/elements")
@RequiredArgsConstructor
public class ElementAdminController {

    private final ElementFacade facade;

    @GetMapping
    public ResponseEntity<List<ElementResponse>> getAll() {
        return ResponseEntity.ok(facade.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ElementResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(facade.getById(id));
    }

    @PostMapping
    public ResponseEntity<ElementResponse> create(@RequestBody @Valid ElementUpsertRequest request) {
        ElementResponse response = facade.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ElementResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid ElementUpsertRequest request
    ) {
        return ResponseEntity.ok(facade.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        facade.delete(id);
        return ResponseEntity.noContent().build();
    }
}
