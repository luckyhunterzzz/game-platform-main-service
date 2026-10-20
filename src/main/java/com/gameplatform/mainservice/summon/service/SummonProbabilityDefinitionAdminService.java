package com.gameplatform.mainservice.summon.service;

import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.summon.domain.entity.SummonProbabilityDefinition;
import com.gameplatform.mainservice.summon.dto.request.SummonProbabilityDefinitionCreateRequest;
import com.gameplatform.mainservice.summon.dto.response.SummonProbabilityDefinitionResponse;
import com.gameplatform.mainservice.summon.repository.SummonProbabilityDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SummonProbabilityDefinitionAdminService {

    private final SummonProbabilityDefinitionRepository repository;

    public List<SummonProbabilityDefinitionResponse> getAll() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(this::sortingName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SummonProbabilityDefinitionResponse create(SummonProbabilityDefinitionCreateRequest request) {
        LocalizedTextJson name = normalizeRequiredText(request.nameJson(), "nameJson");
        LocalizedTextJson description = normalizeOptionalText(request.descriptionJson());
        validateUniqueName(name);

        SummonProbabilityDefinition definition = SummonProbabilityDefinition.builder()
                .nameJson(name)
                .descriptionJson(description)
                .build();

        return toResponse(repository.save(definition));
    }

    private void validateUniqueName(LocalizedTextJson requestedName) {
        for (SummonProbabilityDefinition definition : repository.findAll()) {
            LocalizedTextJson existingName = definition.getNameJson();
            if (sameText(requestedName.ru(), existingName != null ? existingName.ru() : null)
                    || sameText(requestedName.en(), existingName != null ? existingName.en() : null)) {
                throw new BusinessValidationException("Probability definition with this name already exists");
            }
        }
    }

    private boolean sameText(String first, String second) {
        return first != null && second != null
                && first.trim().toLowerCase(Locale.ROOT).equals(second.trim().toLowerCase(Locale.ROOT));
    }

    private String sortingName(SummonProbabilityDefinition definition) {
        LocalizedTextJson name = definition.getNameJson();
        if (name == null) {
            return "";
        }
        if (hasText(name.ru())) {
            return name.ru().trim();
        }
        return hasText(name.en()) ? name.en().trim() : "";
    }

    private SummonProbabilityDefinitionResponse toResponse(SummonProbabilityDefinition definition) {
        return new SummonProbabilityDefinitionResponse(
                definition.getId(),
                definition.getNameJson(),
                definition.getDescriptionJson()
        );
    }

    private LocalizedTextJson normalizeRequiredText(LocalizedTextJson value, String fieldName) {
        LocalizedTextJson normalized = normalizeOptionalText(value);
        if (normalized == null) {
            throw new BusinessValidationException(fieldName + " must contain at least one localized value");
        }
        return normalized;
    }

    private LocalizedTextJson normalizeOptionalText(LocalizedTextJson value) {
        if (value == null) {
            return null;
        }
        String ru = trimToNull(value.ru());
        String en = trimToNull(value.en());
        return ru == null && en == null ? null : new LocalizedTextJson(ru, en);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
