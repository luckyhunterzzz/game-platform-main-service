package com.gameplatform.mainservice.event.controller;

import com.gameplatform.mainservice.event.domain.enums.EventStatus;
import com.gameplatform.mainservice.event.dto.request.EventBlockReorderRequest;
import com.gameplatform.mainservice.event.dto.request.EventBlockUpsertRequest;
import com.gameplatform.mainservice.event.dto.request.EventUpsertRequest;
import com.gameplatform.mainservice.event.dto.response.EventAdminDetailsResponse;
import com.gameplatform.mainservice.event.dto.response.EventAdminSummaryResponse;
import com.gameplatform.mainservice.event.service.EventAdminService;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/events")
@RequiredArgsConstructor
public class AdminEventsController {

    private final EventAdminService eventAdminService;

    @GetMapping
    public CatalogPageResponse<EventAdminSummaryResponse> getCatalog(
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        return eventAdminService.getPage(status, search, page, size);
    }

    @GetMapping("/{slug:[a-z0-9-]+}")
    public EventAdminDetailsResponse getBySlug(@PathVariable String slug) {
        return eventAdminService.getBySlug(slug);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventAdminDetailsResponse create(@RequestBody @Valid EventUpsertRequest request) {
        return eventAdminService.create(request);
    }

    @PutMapping("/{slug:[a-z0-9-]+}")
    public EventAdminDetailsResponse update(
            @PathVariable String slug,
            @RequestBody @Valid EventUpsertRequest request
    ) {
        return eventAdminService.update(slug, request);
    }

    @DeleteMapping("/{slug:[a-z0-9-]+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String slug) {
        eventAdminService.delete(slug);
    }

    @PostMapping("/{slug:[a-z0-9-]+}/blocks")
    @ResponseStatus(HttpStatus.CREATED)
    public EventAdminDetailsResponse createBlock(
            @PathVariable String slug,
            @RequestBody @Valid EventBlockUpsertRequest request
    ) {
        return eventAdminService.createBlock(slug, request);
    }

    @PutMapping("/{slug:[a-z0-9-]+}/blocks/{blockId}")
    public EventAdminDetailsResponse updateBlock(
            @PathVariable String slug,
            @PathVariable Long blockId,
            @RequestBody @Valid EventBlockUpsertRequest request
    ) {
        return eventAdminService.updateBlock(slug, blockId, request);
    }

    @DeleteMapping("/{slug:[a-z0-9-]+}/blocks/{blockId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBlock(
            @PathVariable String slug,
            @PathVariable Long blockId
    ) {
        eventAdminService.deleteBlock(slug, blockId);
    }

    @PutMapping("/{slug:[a-z0-9-]+}/blocks/reorder")
    public EventAdminDetailsResponse reorderBlocks(
            @PathVariable String slug,
            @RequestBody @Valid EventBlockReorderRequest request
    ) {
        return eventAdminService.reorderBlocks(slug, request);
    }
}
