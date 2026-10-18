package com.gameplatform.mainservice.summon.controller;

import com.gameplatform.mainservice.summon.dto.request.SummonProbabilityDefinitionCreateRequest;
import com.gameplatform.mainservice.summon.dto.response.SummonProbabilityDefinitionResponse;
import com.gameplatform.mainservice.summon.service.SummonProbabilityDefinitionAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/summon-probability-definitions")
@RequiredArgsConstructor
public class AdminSummonProbabilityDefinitionsController {

    private final SummonProbabilityDefinitionAdminService service;

    @GetMapping
    public List<SummonProbabilityDefinitionResponse> getAll() {
        return service.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SummonProbabilityDefinitionResponse create(
            @RequestBody @Valid SummonProbabilityDefinitionCreateRequest request
    ) {
        return service.create(request);
    }
}
