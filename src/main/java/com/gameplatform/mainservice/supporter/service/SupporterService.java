package com.gameplatform.mainservice.supporter.service;

import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import com.gameplatform.mainservice.supporter.domain.Supporter;
import com.gameplatform.mainservice.supporter.dto.SupporterResponse;
import com.gameplatform.mainservice.supporter.dto.SupporterUpsertRequest;
import com.gameplatform.mainservice.supporter.repository.SupporterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupporterService {

    private final SupporterRepository supporterRepository;

    @Transactional(readOnly = true)
    public List<SupporterResponse> getPublicSupporters() {
        return supporterRepository.findAllByActiveTrueOrderBySortOrderAscIdAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupporterResponse> getAll() {
        return supporterRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SupporterResponse create(SupporterUpsertRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        Supporter supporter = Supporter.builder()
                .createdAt(now)
                .updatedAt(now)
                .build();
        apply(supporter, request);
        return toResponse(supporterRepository.save(supporter));
    }

    @Transactional
    public SupporterResponse update(Long id, SupporterUpsertRequest request) {
        Supporter supporter = getById(id);
        apply(supporter, request);
        supporter.setUpdatedAt(OffsetDateTime.now());
        return toResponse(supporterRepository.save(supporter));
    }

    @Transactional
    public void delete(Long id) {
        supporterRepository.delete(getById(id));
    }

    private Supporter getById(Long id) {
        return supporterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supporter not found: " + id));
    }

    private void apply(Supporter supporter, SupporterUpsertRequest request) {
        supporter.setNickname(request.nickname().trim());
        supporter.setColor(request.color().toUpperCase());
        supporter.setFontStyle(request.fontStyle());
        supporter.setActive(request.active());
        supporter.setSortOrder(request.sortOrder());
    }

    private SupporterResponse toResponse(Supporter supporter) {
        return new SupporterResponse(
                supporter.getId(),
                supporter.getNickname(),
                supporter.getColor(),
                supporter.getFontStyle(),
                supporter.isActive(),
                supporter.getSortOrder(),
                supporter.getCreatedAt(),
                supporter.getUpdatedAt()
        );
    }
}
