package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.dto.request.HeroCatalogImportRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroCatalogImportResponse;
import com.gameplatform.mainservice.hero.service.HeroImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HeroImportFacade {

    private final HeroImportService heroImportService;

    public HeroCatalogImportResponse importCatalog(HeroCatalogImportRequest request) {
        return heroImportService.importCatalog(request);
    }
}
