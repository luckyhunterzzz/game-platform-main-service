package com.gameplatform.mainservice.publication.controller;

import com.gameplatform.mainservice.publication.domain.enums.PublicationStatus;
import com.gameplatform.mainservice.publication.dto.request.CreatePublicationRequest;
import com.gameplatform.mainservice.publication.dto.response.PublicationFeedResponse;
import com.gameplatform.mainservice.publication.dto.response.PublicationResponse;
import com.gameplatform.mainservice.publication.facade.PublicationAdminFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/publications")
@RequiredArgsConstructor
public class AdminPublicationsController {

    private final PublicationAdminFacade publicationAdminFacade;

    @GetMapping
    public PublicationFeedResponse getFeedByStatus(
            @RequestParam(defaultValue = "PUBLISHED") PublicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        return publicationAdminFacade.getFeedByStatus(status, page, size);
    }

    @GetMapping("/{id}")
    public PublicationResponse getById(@PathVariable UUID id) {
        return publicationAdminFacade.getPublicationById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PublicationResponse create(@RequestBody @Valid CreatePublicationRequest request) {
        return publicationAdminFacade.createPublication(request);
    }
}
