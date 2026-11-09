package com.gameplatform.mainservice.supporter.controller;

import com.gameplatform.mainservice.supporter.dto.SupporterResponse;
import com.gameplatform.mainservice.supporter.service.SupporterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/supporters")
@RequiredArgsConstructor
public class SupporterPublicController {

    private final SupporterService supporterService;

    @GetMapping
    public ResponseEntity<List<SupporterResponse>> getSupporters() {
        return ResponseEntity.ok(supporterService.getPublicSupporters());
    }
}
