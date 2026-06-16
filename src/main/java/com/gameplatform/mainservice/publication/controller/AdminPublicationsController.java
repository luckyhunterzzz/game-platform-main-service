package com.gameplatform.mainservice.publication.controller;

import com.gameplatform.mainservice.publication.domain.enums.PublicationStatus;
import com.gameplatform.mainservice.publication.domain.enums.PublicationType;
import com.gameplatform.mainservice.publication.dto.request.PublicationUpsertRequest;
import com.gameplatform.mainservice.publication.dto.response.PublicationAdminFeedResponse;
import com.gameplatform.mainservice.publication.dto.response.PublicationAdminDetailsResponse;
import com.gameplatform.mainservice.publication.dto.response.PublicationAdminHomeResponse;
import com.gameplatform.mainservice.publication.service.PublicationAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/publications")
@RequiredArgsConstructor
public class AdminPublicationsController {

    private final PublicationAdminService publicationAdminService;

    @GetMapping
    public PublicationAdminFeedResponse getFeedByStatus(
            @RequestParam(defaultValue = "PUBLISHED") PublicationStatus status,
            @RequestParam(required = false) PublicationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        return publicationAdminService.getFeedByStatus(status, type, page, size);
    }

    @GetMapping("/home")
    public PublicationAdminHomeResponse getHomeOverview(
            @RequestParam(required = false) Integer size
    ) {
        return publicationAdminService.getHomeOverview(size);
    }

    @GetMapping("/{id}")
    public PublicationAdminDetailsResponse getById(@PathVariable UUID id) {
        return publicationAdminService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PublicationAdminDetailsResponse create(@RequestBody @Valid PublicationUpsertRequest request) {
        return publicationAdminService.create(request);
    }

    @PutMapping("/{id}")
    public PublicationAdminDetailsResponse update(@PathVariable UUID id, @RequestBody @Valid PublicationUpsertRequest request) {
        return publicationAdminService.update(id, request);
    }
}
