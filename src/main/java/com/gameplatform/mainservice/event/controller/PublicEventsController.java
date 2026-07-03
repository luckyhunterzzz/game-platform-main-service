package com.gameplatform.mainservice.event.controller;

import com.gameplatform.mainservice.event.domain.enums.EventLanguage;
import com.gameplatform.mainservice.event.dto.response.EventFeedResponse;
import com.gameplatform.mainservice.event.dto.response.EventResponse;
import com.gameplatform.mainservice.event.service.EventPublicService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/events")
public class PublicEventsController {

    private final EventPublicService eventPublicService;

    @GetMapping
    public ResponseEntity<EventFeedResponse> getEvents(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size,
            @RequestParam(defaultValue = "RU") EventLanguage language
    ) {
        return ResponseEntity.ok(eventPublicService.getEvents(page, size, language));
    }

    @GetMapping("/{slug:[a-z0-9-]+}")
    public ResponseEntity<EventResponse> getBySlug(
            @PathVariable String slug,
            @RequestParam(defaultValue = "RU") EventLanguage language
    ) {
        return ResponseEntity.ok(eventPublicService.getBySlug(slug, language));
    }
}
