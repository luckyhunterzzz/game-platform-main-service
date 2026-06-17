package com.gameplatform.mainservice.publication.controller;

import com.gameplatform.mainservice.publication.domain.enums.PublicationLanguage;
import com.gameplatform.mainservice.publication.domain.enums.PublicationType;
import com.gameplatform.mainservice.publication.dto.response.PublicationFeedResponse;
import com.gameplatform.mainservice.publication.service.PublicationPublicService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/public/publications")
public class PublicPublicationsController {

    private final PublicationPublicService publicationPublicService;

    @GetMapping
    public ResponseEntity<PublicationFeedResponse> getLatest(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size,
            @RequestParam(defaultValue = "RU") PublicationLanguage language,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PublicationType type
    ) {
        return ResponseEntity.ok(publicationPublicService.getLatestPublicFeed(page, size, language, search, type));
    }

    @GetMapping("/alliances")
    public ResponseEntity<PublicationFeedResponse> getAlliances(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size,
            @RequestParam(defaultValue = "RU") PublicationLanguage language
    ) {
        return ResponseEntity.ok(publicationPublicService.getAllianceFeed(page, size, language));
    }
}
