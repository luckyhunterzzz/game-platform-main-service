package com.gameplatform.mainservice.hero.controller;

import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.dto.response.*;
import com.gameplatform.mainservice.hero.facade.HeroPublicFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/heroes")
@RequiredArgsConstructor
public class HeroPublicController {

    private final HeroPublicFacade heroPublicFacade;

    @GetMapping("/names")
    public ResponseEntity<List<HeroLookupResponse>> getNames(
            @RequestParam(defaultValue = "RU") HeroLanguage language
    ) {
        return ResponseEntity.ok(heroPublicFacade.getNames(language));
    }

    @GetMapping("/search")
    public ResponseEntity<List<HeroLookupResponse>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "RU") HeroLanguage language
    ) {
        return ResponseEntity.ok(heroPublicFacade.search(query, limit, language));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<HeroDetailsResponse> getDetails(
            @PathVariable String slug,
            @RequestParam(defaultValue = "RU") HeroLanguage language ) {
        return ResponseEntity.ok(heroPublicFacade.getDetails(slug, language));
    }

    @GetMapping
    public ResponseEntity<HeroPageResponse> getHeroes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Long> elementIds,
            @RequestParam(required = false) List<Long> rarityIds,
            @RequestParam(required = false) List<Long> heroClassIds,
            @RequestParam(required = false) List<Long> familyIds,
            @RequestParam(required = false) List<Long> manaSpeedIds,
            @RequestParam(required = false) List<Long> alphaTalentIds,
            @RequestParam(defaultValue = "RU") HeroLanguage language
    ) {
        return ResponseEntity.ok(heroPublicFacade.getHeroes(
                page,
                size,
                language,
                search,
                elementIds,
                rarityIds,
                heroClassIds,
                familyIds,
                manaSpeedIds,
                alphaTalentIds
        ));
    }

    @GetMapping("/{slug}/variants")
    public ResponseEntity<HeroVariantsResponse> getVariants(
            @PathVariable String slug,
            @RequestParam(defaultValue = "RU") HeroLanguage language
    ) {
        return ResponseEntity.ok(heroPublicFacade.getVariants(slug, language));
    }
}
