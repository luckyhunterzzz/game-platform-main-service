package com.gameplatform.mainservice.supporter.controller;

import com.gameplatform.mainservice.supporter.dto.SupporterResponse;
import com.gameplatform.mainservice.supporter.dto.SupporterUpsertRequest;
import com.gameplatform.mainservice.supporter.service.SupporterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/supporters")
@RequiredArgsConstructor
public class SupporterAdminController {

    private final SupporterService supporterService;

    @GetMapping
    public ResponseEntity<List<SupporterResponse>> getAll() {
        return ResponseEntity.ok(supporterService.getAll());
    }

    @PostMapping
    public ResponseEntity<SupporterResponse> create(@RequestBody @Valid SupporterUpsertRequest request) {
        SupporterResponse response = supporterService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupporterResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid SupporterUpsertRequest request
    ) {
        return ResponseEntity.ok(supporterService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supporterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
