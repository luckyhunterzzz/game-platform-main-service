package com.gameplatform.mainservice.publication.controller;

import com.gameplatform.mainservice.publication.dto.response.PublicationFeedResponse;
import com.gameplatform.mainservice.publication.facade.PublicationPublicFacade;
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

    private final PublicationPublicFacade publicationPublicFacade;

    @GetMapping
    public ResponseEntity<PublicationFeedResponse> getLatest(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size
    ) {
        return ResponseEntity.ok(publicationPublicFacade.getLatestPublicFeed(page, size));
    }
}