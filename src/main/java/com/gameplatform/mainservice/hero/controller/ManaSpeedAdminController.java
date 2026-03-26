package com.gameplatform.mainservice.hero.controller;

import com.gameplatform.mainservice.hero.dto.request.ManaSpeedCreateRequest;
import com.gameplatform.mainservice.hero.dto.request.ManaSpeedUpdateRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroClassEmblemBonusProfileResponse;
import com.gameplatform.mainservice.hero.dto.response.ManaSpeedResponse;
import com.gameplatform.mainservice.hero.facade.ManaSpeedFacade;
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

    private final ManaSpeedFacade facade;

    @GetMapping
    public ResponseEntity<List<ManaSpeedResponse>> getAll() {
        return ResponseEntity.ok(facade.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManaSpeedResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(facade.getById(id));
    }

    @PostMapping
    public ResponseEntity<ManaSpeedResponse> create(@RequestBody @Valid ManaSpeedCreateRequest request) {
        ManaSpeedResponse response = facade.create(request);

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
            @RequestBody @Valid ManaSpeedUpdateRequest request
    ) {
        return ResponseEntity.ok(facade.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        facade.delete(id);
        return ResponseEntity.noContent().build();
    }
}