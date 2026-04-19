package com.gameplatform.mainservice.media.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.media-migration", name = "enabled", havingValue = "true")
public class MediaMigrationRunner implements ApplicationRunner {

    private final MediaMigrationProperties properties;
    private final MediaMigrationService mediaMigrationService;

    @Override
    public void run(ApplicationArguments args) {
        log.info(
                "Starting media migration: dryRun={}, limit={}",
                properties.isDryRun(),
                properties.getLimit()
        );

        MediaMigrationService.MediaMigrationSummary summary = mediaMigrationService.migrateExistingMedia(
                properties.isDryRun(),
                properties.getLimit()
        );

        log.info(
                "Finished media migration: migrated={}, skipped={}, failed={}",
                summary.migrated(),
                summary.skipped(),
                summary.failed()
        );
    }
}
