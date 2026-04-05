package com.gameplatform.mainservice.publication.controller;

import com.gameplatform.mainservice.publication.domain.enums.PublicationStatus;
import com.gameplatform.mainservice.publication.dto.request.PublicationUpsertRequest;
import com.gameplatform.mainservice.publication.dto.response.PublicationAdminFeedResponse;
import com.gameplatform.mainservice.publication.dto.response.PublicationAdminDetailsResponse;
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
    public PublicationAdminFeedResponse getFeedByStatus(
            @RequestParam(defaultValue = "PUBLISHED") PublicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        return publicationAdminFacade.getFeedByStatus(status, page, size);
    }

    @GetMapping("/{id}")
    public PublicationAdminDetailsResponse getById(@PathVariable UUID id) {
        return publicationAdminFacade.getPublicationById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PublicationAdminDetailsResponse create(@RequestBody @Valid PublicationUpsertRequest request) {
        return publicationAdminFacade.createPublication(request);
    }

    @PutMapping("/{id}")
    public PublicationAdminDetailsResponse update(@PathVariable UUID id, @RequestBody @Valid PublicationUpsertRequest request) {
        return publicationAdminFacade.updatePublication(id, request);
    }
}
