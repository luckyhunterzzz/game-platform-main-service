package com.gameplatform.mainservice.hero.controller;

import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.dto.response.OutfitterForecastResponse;
import com.gameplatform.mainservice.hero.dto.response.OutfitterPageResponse;
import com.gameplatform.mainservice.hero.service.OutfitterPublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/public/outfitter")
@RequiredArgsConstructor
public class OutfitterPublicController {

    private final OutfitterPublicService outfitterPublicService;

    @GetMapping
    public ResponseEntity<OutfitterPageResponse> getAvailableHeroes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "RU") HeroLanguage language
    ) {
        return ResponseEntity.ok(outfitterPublicService.getAvailableHeroes(page, size, language));
    }

    @GetMapping("/forecast")
    public ResponseEntity<OutfitterForecastResponse> getForecast(
            @RequestParam LocalDate targetDate,
            @RequestParam(required = false) LocalDate previousEventDate,
            @RequestParam(defaultValue = "RU") HeroLanguage language
    ) {
        return ResponseEntity.ok(outfitterPublicService.getForecast(targetDate, previousEventDate, language));
    }
}
