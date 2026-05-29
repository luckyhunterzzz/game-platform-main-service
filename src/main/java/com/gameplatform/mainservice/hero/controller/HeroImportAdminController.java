package com.gameplatform.mainservice.hero.controller;

import com.gameplatform.mainservice.hero.dto.request.HeroCatalogImportRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroCatalogImportResponse;
import com.gameplatform.mainservice.hero.facade.HeroImportFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/heroes/import")
@RequiredArgsConstructor
public class HeroImportAdminController {

    private final HeroImportFacade heroImportFacade;

    @PostMapping("/catalog")
    public ResponseEntity<HeroCatalogImportResponse> importCatalog(
            @RequestBody @Valid HeroCatalogImportRequest request
    ) {
        return ResponseEntity.ok(heroImportFacade.importCatalog(request));
    }
}
