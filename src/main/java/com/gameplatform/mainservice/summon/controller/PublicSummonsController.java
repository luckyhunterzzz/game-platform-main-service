package com.gameplatform.mainservice.summon.controller;

import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonCalendarFeedResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonPublicResponse;
import com.gameplatform.mainservice.summon.service.SummonPublicService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/summons")
public class PublicSummonsController {

    private final SummonPublicService summonPublicService;

    @GetMapping
    public List<SummonPublicResponse> getBaseSummons() {
        return summonPublicService.getBaseSummons();
    }

    @GetMapping("/{slug:[a-z0-9-]+}")
    public SummonPublicResponse getBySlug(@PathVariable String slug) {
        return summonPublicService.getBySlug(slug);
    }

    @GetMapping("/calendar")
    public SummonCalendarFeedResponse getCalendar() {
        return summonPublicService.getCalendar();
    }

    @GetMapping("/calendar/history")
    public CatalogPageResponse<SummonPublicResponse> getHistory(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(required = false) @Min(1) @Max(50) Integer size
    ) {
        return summonPublicService.getHistory(page, size);
    }

    @GetMapping("/calendar/{variantId}")
    public SummonPublicResponse getCalendarVariant(@PathVariable UUID variantId) {
        return summonPublicService.getCalendarVariant(variantId);
    }
}
