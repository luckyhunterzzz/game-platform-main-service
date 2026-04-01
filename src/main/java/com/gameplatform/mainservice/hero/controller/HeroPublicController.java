package com.gameplatform.mainservice.hero.controller;

import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.dto.response.HeroDetailsResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroPageResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroSearchResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroSimpleNameResponse;
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
    public ResponseEntity<List<HeroSimpleNameResponse>> getNames(
            @RequestParam(defaultValue = "RU") HeroLanguage language
    ) {
        return ResponseEntity.ok(heroPublicFacade.getNames(language));
    }

    @GetMapping("/search")
    public ResponseEntity<List<HeroSearchResponse>> search(
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
            @RequestParam(defaultValue = "RU") HeroLanguage language
    ) {
        return ResponseEntity.ok(heroPublicFacade.getHeroes(page, size, language));
    }
}