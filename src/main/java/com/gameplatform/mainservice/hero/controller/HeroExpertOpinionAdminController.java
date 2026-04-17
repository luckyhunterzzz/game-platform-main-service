package com.gameplatform.mainservice.hero.controller;

import com.gameplatform.mainservice.hero.dto.request.HeroExpertOpinionUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroExpertOpinionAdminResponse;
import com.gameplatform.mainservice.hero.facade.HeroExpertOpinionAdminFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/heroes/{heroId}/expert-opinions")
@RequiredArgsConstructor
public class HeroExpertOpinionAdminController {

    private final HeroExpertOpinionAdminFacade facade;

    @GetMapping
    public ResponseEntity<List<HeroExpertOpinionAdminResponse>> getAll(@PathVariable Long heroId) {
        return ResponseEntity.ok(facade.getAllByHeroId(heroId));
    }

    @PostMapping
    public ResponseEntity<HeroExpertOpinionAdminResponse> create(
            @PathVariable Long heroId,
            @RequestBody @Valid HeroExpertOpinionUpsertRequest request
    ) {
        HeroExpertOpinionAdminResponse response = facade.create(heroId, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{opinionId}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{opinionId}")
    public ResponseEntity<HeroExpertOpinionAdminResponse> update(
            @PathVariable Long heroId,
            @PathVariable Long opinionId,
            @RequestBody @Valid HeroExpertOpinionUpsertRequest request
    ) {
        return ResponseEntity.ok(facade.update(heroId, opinionId, request));
    }

    @DeleteMapping("/{opinionId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long heroId,
            @PathVariable Long opinionId
    ) {
        facade.delete(heroId, opinionId);
        return ResponseEntity.noContent().build();
    }
}
