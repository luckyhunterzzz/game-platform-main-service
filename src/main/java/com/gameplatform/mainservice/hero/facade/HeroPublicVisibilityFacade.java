package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.dto.request.HeroPublicVisibilityUpdateRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroPublicVisibilityResponse;
import com.gameplatform.mainservice.hero.service.HeroPublicVisibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HeroPublicVisibilityFacade {

    private final HeroPublicVisibilityService heroPublicVisibilityService;

    public HeroPublicVisibilityResponse getVisibility() {
        return heroPublicVisibilityService.getVisibility();
    }

    public HeroPublicVisibilityResponse updateVisibility(HeroPublicVisibilityUpdateRequest request) {
        return heroPublicVisibilityService.updateVisibility(request);
    }
}
