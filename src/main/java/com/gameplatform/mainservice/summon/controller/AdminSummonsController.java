package com.gameplatform.mainservice.summon.controller;

import com.gameplatform.mainservice.summon.dto.request.SummonCreateRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonVariantCloneRequest;
import com.gameplatform.mainservice.summon.dto.response.SummonAdminDetailsResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonAdminSummaryResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonVariantResponse;
import com.gameplatform.mainservice.summon.service.SummonAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/summons")
@RequiredArgsConstructor
public class AdminSummonsController {

    private final SummonAdminService summonAdminService;

    @GetMapping
    public List<SummonAdminSummaryResponse> getAll() {
        return summonAdminService.getAll();
    }

    @GetMapping("/{summonId}")
    public SummonAdminDetailsResponse getById(@PathVariable UUID summonId) {
        return summonAdminService.getById(summonId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SummonAdminDetailsResponse create(@RequestBody @Valid SummonCreateRequest request) {
        return summonAdminService.create(request);
    }

    @PutMapping("/{summonId}")
    public SummonAdminDetailsResponse update(@PathVariable UUID summonId,
                                             @RequestBody @Valid SummonCreateRequest request) {
        return summonAdminService.update(summonId, request);
    }

    @PostMapping("/{summonId}/variants/clone")
    @ResponseStatus(HttpStatus.CREATED)
    public SummonVariantResponse cloneVariant(@PathVariable UUID summonId,
                                              @RequestBody @Valid SummonVariantCloneRequest request) {
        return summonAdminService.cloneVariant(summonId, request);
    }
}
