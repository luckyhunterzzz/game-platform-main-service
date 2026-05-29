package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.dto.external.ExternalHeroCatalogPayload;
import com.gameplatform.mainservice.hero.dto.external.ExternalHeroRecord;
import com.gameplatform.mainservice.hero.dto.external.ExternalLocalizedHeroRecord;
import com.gameplatform.mainservice.hero.dto.request.HeroCatalogImportRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroCatalogImportPlannedHeroResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroCatalogImportResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroCatalogImportSkippedItemResponse;
import com.gameplatform.mainservice.hero.service.importer.HeroImportCatalogClient;
import com.gameplatform.mainservice.hero.service.importer.HeroImportDictionaryLookup;
import com.gameplatform.mainservice.hero.service.importer.HeroImportDictionaryLookupService;
import com.gameplatform.mainservice.hero.service.importer.HeroImportHeroProcessingResult;
import com.gameplatform.mainservice.hero.service.importer.HeroImportHeroProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class HeroImportService {

    private static final int MAX_DETAILS = 100;
    private static final int IMPORT_BATCH_SIZE = 32;
    private static final int IMPORT_THREAD_POOL_SIZE = 8;

    private final HeroImportCatalogClient catalogClient;
    private final HeroImportDictionaryLookupService dictionaryLookupService;
    private final HeroImportHeroProcessor heroProcessor;

    public HeroCatalogImportResponse importCatalog(HeroCatalogImportRequest request) {
        ExternalHeroCatalogPayload payload = catalogClient.downloadCatalog(request.sourceUrl());
        List<ExternalHeroRecord> sourceHeroes = payload.allHeroes() == null ? List.of() : payload.allHeroes();
        boolean dryRun = isDryRun(request);

        List<ExternalHeroRecord> matchedHeroes = sourceHeroes.stream()
                .filter(Objects::nonNull)
                .filter(hero -> Objects.equals(hero.star(), request.star()))
                .filter(hero -> heroProcessor.matchesParentMode(hero, request.parentMode()))
                .filter(hero -> heroProcessor.matchesReleaseDateRange(hero, request))
                .toList();

        HeroImportDictionaryLookup lookup = dictionaryLookupService.buildLookup();
        LocalizedHeroIndex localizedHeroIndex = buildLocalizedHeroIndex(request);
        ImportAggregation aggregation = new ImportAggregation();

        ExecutorService executor = Executors.newFixedThreadPool(
                IMPORT_THREAD_POOL_SIZE,
                new HeroImportThreadFactory()
        );

        try {
            List<List<ExternalHeroRecord>> batches = partition(matchedHeroes, IMPORT_BATCH_SIZE);
            int processedHeroes = 0;

            for (int batchIndex = 0; batchIndex < batches.size(); batchIndex++) {
                List<ExternalHeroRecord> batch = batches.get(batchIndex);
                List<CompletableFuture<HeroImportHeroProcessingResult>> futures = batch.stream()
                        .map(hero -> CompletableFuture.supplyAsync(
                                () -> heroProcessor.process(
                                        hero,
                                        resolveLocalizedHero(hero, localizedHeroIndex),
                                        request,
                                        dryRun,
                                        lookup
                                ),
                                executor
                        ))
                        .toList();

                List<HeroImportHeroProcessingResult> results = futures.stream()
                        .map(CompletableFuture::join)
                        .toList();

                for (HeroImportHeroProcessingResult result : results) {
                    aggregation.accept(result);
                }

                processedHeroes += batch.size();
                log.info(
                        "Hero catalog import batch finished. dryRun={}, batch={}/{}, batchSize={}, processedHeroes={}, matchedHeroes={}, createdHeroes={}, skippedExistingHeroes={}, skippedUnresolvedHeroes={}",
                        dryRun,
                        batchIndex + 1,
                        batches.size(),
                        batch.size(),
                        processedHeroes,
                        matchedHeroes.size(),
                        aggregation.createdHeroes,
                        aggregation.skippedExistingHeroes,
                        aggregation.skippedUnresolvedHeroes
                );
            }
        } finally {
            executor.shutdown();
        }

        log.info(
                "Hero catalog import finished. dryRun={}, matchedHeroes={}, createdHeroes={}, skippedExistingHeroes={}, skippedUnresolvedHeroes={}, batchSize={}, threadPoolSize={}",
                dryRun,
                matchedHeroes.size(),
                aggregation.createdHeroes,
                aggregation.skippedExistingHeroes,
                aggregation.skippedUnresolvedHeroes,
                IMPORT_BATCH_SIZE,
                IMPORT_THREAD_POOL_SIZE
        );

        return new HeroCatalogImportResponse(
                dryRun,
                sourceHeroes.size(),
                matchedHeroes.size(),
                aggregation.createdHeroes,
                aggregation.skippedExistingHeroes,
                aggregation.skippedUnresolvedHeroes,
                List.copyOf(aggregation.createdSlugs),
                List.copyOf(aggregation.plannedHeroes),
                List.copyOf(aggregation.skippedHeroes)
        );
    }

    private boolean isDryRun(HeroCatalogImportRequest request) {
        return request.dryRun() == null || request.dryRun();
    }

    private LocalizedHeroIndex buildLocalizedHeroIndex(HeroCatalogImportRequest request) {
        String localizedSourceUrl = trimToNull(request.localizedSourceUrl());
        if (localizedSourceUrl == null) {
            return new LocalizedHeroIndex(Map.of(), Map.of());
        }

        Map<String, ExternalLocalizedHeroRecord> localizedCatalog = catalogClient.downloadLocalizedCatalog(localizedSourceUrl);
        Map<String, ExternalLocalizedHeroRecord> localizedHeroesByHeroId = new HashMap<>();
        Map<String, ExternalLocalizedHeroRecord> localizedHeroesBySlug = new HashMap<>();
        for (ExternalLocalizedHeroRecord localizedHero : localizedCatalog.values()) {
            if (localizedHero == null) {
                continue;
            }

            String localizedHeroId = trimToNull(localizedHero.empuzzledHeroId());
            if (localizedHeroId != null) {
                localizedHeroesByHeroId.putIfAbsent(localizedHeroId, localizedHero);
            }

            String localizedSlug = buildSlugKey(localizedHero.empuzzledName());
            if (localizedSlug != null) {
                localizedHeroesBySlug.putIfAbsent(localizedSlug, localizedHero);
            }
        }
        return new LocalizedHeroIndex(localizedHeroesByHeroId, localizedHeroesBySlug);
    }

    private ExternalLocalizedHeroRecord resolveLocalizedHero(ExternalHeroRecord hero, LocalizedHeroIndex localizedHeroIndex) {
        if (hero == null) {
            return null;
        }

        String heroId = trimToNull(hero.heroId());
        if (heroId != null) {
            ExternalLocalizedHeroRecord byHeroId = localizedHeroIndex.byHeroId().get(heroId);
            if (byHeroId != null) {
                return byHeroId;
            }
        }

        return localizedHeroIndex.bySlug().get(buildSlugKey(hero));
    }

    private String buildSlugKey(ExternalHeroRecord hero) {
        return hero == null ? null : buildSlugKey(hero.name());
    }

    private String buildSlugKey(String sourceName) {
        return heroProcessor.buildBaseSlug(sourceName);
    }

    private List<List<ExternalHeroRecord>> partition(List<ExternalHeroRecord> heroes, int batchSize) {
        if (heroes.isEmpty()) {
            return List.of();
        }

        List<List<ExternalHeroRecord>> batches = new ArrayList<>();
        for (int start = 0; start < heroes.size(); start += batchSize) {
            int end = Math.min(start + batchSize, heroes.size());
            batches.add(heroes.subList(start, end));
        }
        return batches;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String normalized = trimToNull(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private static final class ImportAggregation {
        private int createdHeroes;
        private int skippedExistingHeroes;
        private int skippedUnresolvedHeroes;
        private final List<String> createdSlugs = new ArrayList<>();
        private final List<HeroCatalogImportPlannedHeroResponse> plannedHeroes = new ArrayList<>();
        private final List<HeroCatalogImportSkippedItemResponse> skippedHeroes = new ArrayList<>();

        private void accept(HeroImportHeroProcessingResult result) {
            if (result == null) {
                return;
            }

            if (result.isCreated()) {
                createdHeroes++;
                if (result.slug() != null) {
                    createdSlugs.add(result.slug());
                }
                if (result.plannedHero() != null) {
                    plannedHeroes.add(result.plannedHero());
                }
                return;
            }

            if (result.isSkippedExisting()) {
                skippedExistingHeroes++;
            } else if (result.isSkippedUnresolved()) {
                skippedUnresolvedHeroes++;
            }

            if (result.skippedHero() != null && skippedHeroes.size() < MAX_DETAILS) {
                skippedHeroes.add(result.skippedHero());
            }
        }
    }

    private static final class HeroImportThreadFactory implements ThreadFactory {
        private final AtomicInteger sequence = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("hero-import-" + sequence.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }

    private record LocalizedHeroIndex(
            Map<String, ExternalLocalizedHeroRecord> byHeroId,
            Map<String, ExternalLocalizedHeroRecord> bySlug
    ) {
    }
}
