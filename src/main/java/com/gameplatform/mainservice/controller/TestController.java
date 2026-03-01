package com.gameplatform.mainservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Diagnostic controller using Lombok for logging and ResponseEntity for HTTP control.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class TestController {
    private static final String MDC_REQUEST_ID = "requestId";

    /**
     * Public diagnostic endpoint returning ResponseEntity.
     */
    @GetMapping("/public/test")
    public ResponseEntity<Map<String, Object>> publicTest() {
        log.info("public diagnostic test");

        Map<String, Object> body = new HashMap<>();
        body.put("status", "UP");
        body.put("service", "main-service");
        body.put("requestId", MDC.get("requestId") != null ? MDC.get("requestId") : "not-traced");
        return ResponseEntity.ok(body);
    }

    /**
     * Protected diagnostic endpoint for administrators.
     */
    @GetMapping("/admin/test")
    public ResponseEntity<Map<String, Object>> adminTest(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Access denied: Authentication context is missing");
            throw new AccessDeniedException("Access denied: Authentication context is missing");
        }

        log.info("admin diagnostic test");

        List<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        if (authorities.isEmpty()) {
            log.warn("Access denied: User has no authorities");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("userId", auth.getName());
        body.put("roles", authorities);
        body.put("requestId", Optional.ofNullable(MDC.get(MDC_REQUEST_ID)).orElse("not-traced"));
        body.put("authCheck", Map.of(
                "isAdmin", authorities.contains("ROLE_admin"),
                "isSuperadmin", authorities.contains("ROLE_superadmin")
        ));
        body.put("timestamp", OffsetDateTime.now());

        return ResponseEntity.ok(body);
    }
}
