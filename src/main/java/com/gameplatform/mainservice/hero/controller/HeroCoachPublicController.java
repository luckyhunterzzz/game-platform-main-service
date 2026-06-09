package com.gameplatform.mainservice.hero.controller;

import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.dto.response.HeroCoachForecastResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroCoachPageResponse;
import com.gameplatform.mainservice.hero.service.HeroCoachPublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/public/hero-coach")
@RequiredArgsConstructor
public class HeroCoachPublicController {

    private final HeroCoachPublicService heroCoachPublicService;

    @GetMapping
    public ResponseEntity<HeroCoachPageResponse> getAvailableHeroes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "RU") HeroLanguage language
    ) {
        return ResponseEntity.ok(heroCoachPublicService.getAvailableHeroes(page, size, language));
    }

    @GetMapping("/forecast")
    public ResponseEntity<HeroCoachForecastResponse> getForecast(
            @RequestParam LocalDate targetDate,
            @RequestParam(required = false) LocalDate previousEventDate,
            @RequestParam(defaultValue = "RU") HeroLanguage language
    ) {
        return ResponseEntity.ok(heroCoachPublicService.getForecast(targetDate, previousEventDate, language));
    }
}
