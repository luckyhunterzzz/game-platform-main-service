package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.domain.enums.HeroPublicVisibilityMode;
import com.gameplatform.mainservice.hero.dto.request.HeroPublicVisibilityUpdateRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroPublicVisibilityResponse;
import com.gameplatform.mainservice.settings.domain.entity.AppSetting;
import com.gameplatform.mainservice.settings.repository.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class HeroPublicVisibilityService {

    private static final String HERO_PUBLIC_VISIBILITY_KEY = "hero_public_visibility";

    private final AppSettingRepository appSettingRepository;

    public HeroPublicVisibilityResponse getVisibility() {
        return toResponse(getOrCreateSetting());
    }

    public HeroPublicVisibilityResponse updateVisibility(HeroPublicVisibilityUpdateRequest request) {
        ensureSuperAdmin();

        AppSetting setting = getOrCreateSetting();
        setting.setSettingValue(request.mode().name());
        setting.setUpdatedAt(OffsetDateTime.now());
        setting.setUpdatedBy(request.updatedBy().trim());
        setting.setUpdatedByEmail(trimToNull(request.updatedByEmail()));

        return toResponse(appSettingRepository.save(setting));
    }

    public boolean isDraftVisibleInPublicCatalog() {
        return resolveMode(getOrCreateSetting()) == HeroPublicVisibilityMode.READY_AND_DRAFT;
    }

    private AppSetting getOrCreateSetting() {
        return appSettingRepository.findById(HERO_PUBLIC_VISIBILITY_KEY)
                .orElseGet(() -> appSettingRepository.save(
                        AppSetting.builder()
                                .settingKey(HERO_PUBLIC_VISIBILITY_KEY)
                                .settingValue(HeroPublicVisibilityMode.READY_ONLY.name())
                                .updatedAt(OffsetDateTime.now())
                                .build()
                ));
    }

    private HeroPublicVisibilityMode resolveMode(AppSetting setting) {
        try {
            return HeroPublicVisibilityMode.valueOf(setting.getSettingValue());
        } catch (IllegalArgumentException | NullPointerException ex) {
            return HeroPublicVisibilityMode.READY_ONLY;
        }
    }

    private HeroPublicVisibilityResponse toResponse(AppSetting setting) {
        HeroPublicVisibilityMode mode = resolveMode(setting);
        return new HeroPublicVisibilityResponse(
                mode,
                mode == HeroPublicVisibilityMode.READY_AND_DRAFT,
                setting.getUpdatedAt(),
                setting.getUpdatedBy(),
                setting.getUpdatedByEmail()
        );
    }

    private void ensureSuperAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            throw new AccessDeniedException("Only superadmin can change public hero visibility");
        }

        boolean superAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_superadmin".equals(authority.getAuthority()));

        if (!superAdmin) {
            throw new AccessDeniedException("Only superadmin can change public hero visibility");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
