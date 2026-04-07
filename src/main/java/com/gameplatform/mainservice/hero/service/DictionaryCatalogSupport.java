package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

@Component
public class DictionaryCatalogSupport {

    public <T> List<T> sortLocalized(List<T> items, Function<T, LocalizedTextJson> extractor) {
        return items.stream()
                .sorted(Comparator
                        .comparing((T item) -> sortableLocalized(extractor.apply(item)))
                        .thenComparingInt(items::indexOf))
                .toList();
    }

    public <T> Page<T> pageLocalized(List<T> items, String search, int page, int size, Function<T, LocalizedTextJson> extractor) {
        List<T> filtered = items.stream()
                .filter(item -> matchesLocalized(extractor.apply(item), search))
                .sorted(Comparator
                        .comparing((T item) -> sortableLocalized(extractor.apply(item)))
                        .thenComparingInt(items::indexOf))
                .toList();

        return toPage(filtered, page, size);
    }

    public <T> Page<T> toPage(List<T> items, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        int fromIndex = Math.min(safePage * safeSize, items.size());
        int toIndex = Math.min(fromIndex + safeSize, items.size());
        return new PageImpl<>(
                items.subList(fromIndex, toIndex),
                PageRequest.of(safePage, safeSize),
                items.size()
        );
    }

    public boolean matchesLocalized(LocalizedTextJson value, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }

        if (value == null) {
            return false;
        }

        String normalizedSearch = normalize(search);
        return normalize(value.ru()).contains(normalizedSearch)
                || normalize(value.en()).contains(normalizedSearch);
    }

    public String sortableLocalized(LocalizedTextJson value) {
        if (value == null) {
            return "";
        }

        String ru = normalize(value.ru());
        if (!ru.isBlank()) {
            return ru;
        }

        return normalize(value.en());
    }

    public String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
