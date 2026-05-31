package com.gameplatform.mainservice.hero.controller;

import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.dto.request.HeroBatchLookupRequest;
import com.gameplatform.mainservice.hero.dto.request.HeroStatCalculationRequest;
import com.gameplatform.mainservice.hero.dto.response.*;
import jakarta.validation.Valid;
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

    @PostMapping("/batch")
    public ResponseEntity<List<HeroCardResponse>> getHeroesBatch(
            @RequestParam(defaultValue = "RU") HeroLanguage language,
            @RequestParam(defaultValue = "false") boolean includeDrafts,
            @RequestBody @Valid HeroBatchLookupRequest request
    ) {
        return ResponseEntity.ok(heroPublicFacade.getHeroesBatch(language, includeDrafts, request));
    }

    @GetMapping("/search")
    public ResponseEntity<List<HeroLookupResponse>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "RU") HeroLanguage language
    ) {
        return ResponseEntity.ok(heroPublicFacade.search(query, limit, language));
    }

    @GetMapping("/filters")
    public ResponseEntity<HeroCatalogFiltersResponse> getFilters(
            @RequestParam(defaultValue = "RU") HeroLanguage language
    ) {
        return ResponseEntity.ok(heroPublicFacade.getFilters(language));
    }

    @GetMapping("/{slug:^(?!filters$|search$|names$)[a-z0-9-]+$}")
    public ResponseEntity<HeroDetailsResponse> getDetails(
            @PathVariable String slug,
            @RequestParam(defaultValue = "RU") HeroLanguage language,
            @RequestParam(defaultValue = "false") boolean includeDrafts ) {
        return ResponseEntity.ok(heroPublicFacade.getDetails(slug, language, includeDrafts));
    }

    @PostMapping("/{slug:^(?!filters$|search$|names$)[a-z0-9-]+$}/stats/calculate")
    public ResponseEntity<HeroStatCalculationResponse> calculateStats(
            @PathVariable String slug,
            @RequestParam(defaultValue = "RU") HeroLanguage language,
            @RequestParam(defaultValue = "false") boolean includeDrafts,
            @RequestBody @Valid HeroStatCalculationRequest request
    ) {
        return ResponseEntity.ok(heroPublicFacade.calculateStats(slug, language, includeDrafts, request));
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
            @RequestParam(defaultValue = "false") boolean includeDrafts,
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
                alphaTalentIds,
                includeDrafts
        ));
    }

    @GetMapping("/{slug:^(?!filters$|search$|names$)[a-z0-9-]+$}/variants")
    public ResponseEntity<HeroVariantsResponse> getVariants(
            @PathVariable String slug,
            @RequestParam(defaultValue = "RU") HeroLanguage language,
            @RequestParam(defaultValue = "false") boolean includeDrafts
    ) {
        return ResponseEntity.ok(heroPublicFacade.getVariants(slug, language, includeDrafts));
    }
}
