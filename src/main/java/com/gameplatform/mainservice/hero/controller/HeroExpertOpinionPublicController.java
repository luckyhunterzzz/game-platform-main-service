package com.gameplatform.mainservice.hero.controller;

import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.dto.response.HeroExpertOpinionPublicResponse;
import com.gameplatform.mainservice.hero.facade.HeroExpertOpinionPublicFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/heroes/{slug:^(?!filters$|search$|names$)[a-z0-9-]+$}/expert-opinions")
@RequiredArgsConstructor
public class HeroExpertOpinionPublicController {

    private final HeroExpertOpinionPublicFacade facade;

    @GetMapping
    public ResponseEntity<List<HeroExpertOpinionPublicResponse>> getAll(
            @PathVariable String slug,
            @RequestParam(defaultValue = "RU") HeroLanguage language
    ) {
        return ResponseEntity.ok(facade.getAllByHeroSlug(slug, language));
    }
}
