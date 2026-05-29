package com.gameplatform.mainservice.hero.controller;

import com.gameplatform.mainservice.hero.dto.request.HeroPublicVisibilityUpdateRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroPublicVisibilityResponse;
import com.gameplatform.mainservice.hero.facade.HeroPublicVisibilityFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/heroes/public-visibility")
@RequiredArgsConstructor
public class HeroPublicVisibilityAdminController {

    private final HeroPublicVisibilityFacade heroPublicVisibilityFacade;

    @GetMapping
    public ResponseEntity<HeroPublicVisibilityResponse> getVisibility() {
        return ResponseEntity.ok(heroPublicVisibilityFacade.getVisibility());
    }

    @PutMapping
    public ResponseEntity<HeroPublicVisibilityResponse> updateVisibility(
            @RequestBody @Valid HeroPublicVisibilityUpdateRequest request
    ) {
        return ResponseEntity.ok(heroPublicVisibilityFacade.updateVisibility(request));
    }
}
