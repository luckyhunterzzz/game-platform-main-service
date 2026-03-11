package com.gameplatform.mainservice.publication.controller;

import com.gameplatform.mainservice.publication.dto.request.CreatePublicationRequest;
import com.gameplatform.mainservice.publication.dto.response.PublicationResponse;
import com.gameplatform.mainservice.publication.facade.PublicationAdminFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/publications")
@RequiredArgsConstructor
public class AdminPublicationsController {

    private final PublicationAdminFacade publicationAdminFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PublicationResponse create(@RequestBody @Valid CreatePublicationRequest request) {
        return publicationAdminFacade.createPublication(request);
    }
}
