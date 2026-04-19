package com.gameplatform.mainservice.media.migration;

import com.gameplatform.mainservice.hero.domain.entity.*;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.hero.repository.*;
import com.gameplatform.mainservice.media.service.WebpImageConverter;
import com.gameplatform.mainservice.publication.domain.entity.Publication;
import com.gameplatform.mainservice.publication.repository.PublicationRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaMigrationService {

    private final HeroRepository heroRepository;
    private final ElementRepository elementRepository;
    private final FamilyRepository familyRepository;
    private final HeroClassRepository heroClassRepository;
    private final RarityRepository rarityRepository;
    private final AlphaTalentRepository alphaTalentRepository;
    private final PassiveSkillRepository passiveSkillRepository;
    private final PublicationRepository publicationRepository;
    private final MinioClient minioClient;
    private final WebpImageConverter webpImageConverter;

    public MediaMigrationSummary migrateExistingMedia(boolean dryRun, int limit) {
        MigrationCounters counters = new MigrationCounters(limit);
        Map<String, MigrationTarget> cache = new HashMap<>();

        for (Hero hero : heroRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))) {
            if (counters.limitReached()) {
                return counters.toSummary();
            }
            migrateHero(hero, dryRun, counters, cache);
        }

        migrateSimpleEntities("element", elementRepository.findAll(Sort.by(Sort.Direction.ASC, "id")), dryRun, counters, cache);
        migrateSimpleEntities("family", familyRepository.findAll(Sort.by(Sort.Direction.ASC, "id")), dryRun, counters, cache);
        migrateSimpleEntities("heroClass", heroClassRepository.findAll(Sort.by(Sort.Direction.ASC, "id")), dryRun, counters, cache);
        migrateSimpleEntities("rarity", rarityRepository.findAll(Sort.by(Sort.Direction.ASC, "id")), dryRun, counters, cache);
        migrateSimpleEntities("alphaTalent", alphaTalentRepository.findAll(Sort.by(Sort.Direction.ASC, "id")), dryRun, counters, cache);
        migrateSimpleEntities("passiveSkill", passiveSkillRepository.findAll(Sort.by(Sort.Direction.ASC, "id")), dryRun, counters, cache);

        for (Publication publication : publicationRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))) {
            if (counters.limitReached()) {
                return counters.toSummary();
            }

            MigrationTarget target = migrateReference(
                    "publication",
                    publication.getId().toString(),
                    publication.getImageBucket(),
                    publication.getImageObjectKey(),
                    dryRun,
                    counters,
                    cache
            );

            if (!dryRun && target != null) {
                publication.setImageObjectKey(target.objectKey());
                publicationRepository.save(publication);
            }
        }

        return counters.toSummary();
    }

    private void migrateHero(Hero hero, boolean dryRun, MigrationCounters counters, Map<String, MigrationTarget> cache) {
        MigrationTarget ruTarget = migrateReference("hero.image.ru", hero.getId().toString(), imageBucket(hero.getImageBucketJson(), true), imageObjectKey(hero.getImageObjectKeyJson(), true), dryRun, counters, cache);
        MigrationTarget enTarget = migrateReference("hero.image.en", hero.getId().toString(), imageBucket(hero.getImageBucketJson(), false), imageObjectKey(hero.getImageObjectKeyJson(), false), dryRun, counters, cache);
        MigrationTarget previewTarget = migrateReference("hero.preview", hero.getId().toString(), hero.getPreviewBucket(), hero.getPreviewObjectKey(), dryRun, counters, cache);

        if (dryRun) {
            return;
        }

        boolean changed = false;

        if (ruTarget != null || enTarget != null) {
            LocalizedTextJson currentBuckets = hero.getImageBucketJson();
            LocalizedTextJson currentObjectKeys = hero.getImageObjectKeyJson();

            String updatedRuBucket = ruTarget != null ? ruTarget.bucket() : imageBucket(currentBuckets, true);
            String updatedEnBucket = enTarget != null ? enTarget.bucket() : imageBucket(currentBuckets, false);
            String updatedRuObjectKey = ruTarget != null ? ruTarget.objectKey() : imageObjectKey(currentObjectKeys, true);
            String updatedEnObjectKey = enTarget != null ? enTarget.objectKey() : imageObjectKey(currentObjectKeys, false);

            hero.setImageBucketJson(new LocalizedTextJson(updatedRuBucket, updatedEnBucket));
            hero.setImageObjectKeyJson(new LocalizedTextJson(updatedRuObjectKey, updatedEnObjectKey));
            changed = true;
        }

        if (previewTarget != null) {
            hero.setPreviewBucket(previewTarget.bucket());
            hero.setPreviewObjectKey(previewTarget.objectKey());
            changed = true;
        }

        if (changed) {
            heroRepository.save(hero);
        }
    }

    private <T extends SimpleImageEntity> void migrateSimpleEntities(
            String entityType,
            Iterable<T> entities,
            boolean dryRun,
            MigrationCounters counters,
            Map<String, MigrationTarget> cache
    ) {
        for (T entity : entities) {
            if (counters.limitReached()) {
                return;
            }

            MigrationTarget target = migrateReference(
                    entityType,
                    entity.getId().toString(),
                    entity.getImageBucket(),
                    entity.getImageObjectKey(),
                    dryRun,
                    counters,
                    cache
            );

            if (!dryRun && target != null) {
                entity.setImageBucket(target.bucket());
                entity.setImageObjectKey(target.objectKey());
                saveSimpleEntity(entity);
            }
        }
    }

    private void saveSimpleEntity(SimpleImageEntity entity) {
        if (entity instanceof Element value) {
            elementRepository.save(value);
            return;
        }
        if (entity instanceof Family value) {
            familyRepository.save(value);
            return;
        }
        if (entity instanceof HeroClass value) {
            heroClassRepository.save(value);
            return;
        }
        if (entity instanceof Rarity value) {
            rarityRepository.save(value);
            return;
        }
        if (entity instanceof AlphaTalent value) {
            alphaTalentRepository.save(value);
            return;
        }
        if (entity instanceof PassiveSkill value) {
            passiveSkillRepository.save(value);
        }
    }

    private MigrationTarget migrateReference(
            String entityType,
            String entityId,
            String bucket,
            String objectKey,
            boolean dryRun,
            MigrationCounters counters,
            Map<String, MigrationTarget> cache
    ) {
        if (counters.limitReached()) {
            return null;
        }

        if (bucket == null || bucket.isBlank() || objectKey == null || objectKey.isBlank()) {
            counters.skipped++;
            return null;
        }

        String contentType = webpImageConverter.resolveContentTypeByObjectKey(objectKey);
        if (contentType == null) {
            counters.skipped++;
            log.warn("Skipping {}:{} because file extension is unsupported: {}", entityType, entityId, objectKey);
            return null;
        }

        if ("image/webp".equals(contentType)) {
            counters.skipped++;
            return null;
        }

        String sourceRef = bucket + "/" + objectKey;
        if (cache.containsKey(sourceRef)) {
            counters.migrated++;
            return cache.get(sourceRef);
        }

        try {
            String targetObjectKey = replaceExtensionWithWebp(objectKey);
            if (dryRun) {
                log.info("Dry-run migration {}:{} {} -> {}", entityType, entityId, objectKey, targetObjectKey);
                MigrationTarget target = new MigrationTarget(bucket, targetObjectKey);
                cache.put(sourceRef, target);
                counters.migrated++;
                return target;
            }

            byte[] sourceBytes = downloadObject(bucket, objectKey);
            WebpImageConverter.ConvertedImage convertedImage = webpImageConverter.convert(sourceBytes, contentType);
            uploadObject(bucket, targetObjectKey, convertedImage.bytes(), convertedImage.contentType());

            log.info("Migrated {}:{} {} -> {}", entityType, entityId, objectKey, targetObjectKey);

            MigrationTarget target = new MigrationTarget(bucket, targetObjectKey);
            cache.put(sourceRef, target);
            counters.migrated++;
            return target;
        } catch (Exception e) {
            counters.failed++;
            log.error("Failed to migrate {}:{} with objectKey={}", entityType, entityId, objectKey, e);
            return null;
        }
    }

    private byte[] downloadObject(String bucket, String objectKey) throws Exception {
        try (var inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .build()
        )) {
            return inputStream.readAllBytes();
        }
    }

    private void uploadObject(String bucket, String objectKey, byte[] bytes, String contentType) throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(inputStream, bytes.length, -1)
                            .contentType(contentType)
                            .build()
            );
        }
    }

    private String replaceExtensionWithWebp(String objectKey) {
        int extensionIndex = objectKey.lastIndexOf('.');
        if (extensionIndex < 0) {
            return objectKey + ".webp";
        }
        return objectKey.substring(0, extensionIndex) + ".webp";
    }

    private String imageBucket(LocalizedTextJson value, boolean ru) {
        return value == null ? null : (ru ? value.ru() : value.en());
    }

    private String imageObjectKey(LocalizedTextJson value, boolean ru) {
        return value == null ? null : (ru ? value.ru() : value.en());
    }

    public record MediaMigrationSummary(int migrated, int skipped, int failed) {
    }

    private record MigrationTarget(String bucket, String objectKey) {
    }

    private static class MigrationCounters {
        private final int limit;
        private int migrated;
        private int skipped;
        private int failed;

        private MigrationCounters(int limit) {
            this.limit = limit;
        }

        private boolean limitReached() {
            return limit > 0 && migrated >= limit;
        }

        private MediaMigrationSummary toSummary() {
            return new MediaMigrationSummary(migrated, skipped, failed);
        }
    }
}
