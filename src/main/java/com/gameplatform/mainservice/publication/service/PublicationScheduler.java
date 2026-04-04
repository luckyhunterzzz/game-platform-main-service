package com.gameplatform.mainservice.publication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicationScheduler {

    private final PublicationAdminService publicationAdminService;

    @Scheduled(cron = "${publication.scheduler.publish-cron:0 0/15 * * * *}")
    public void publishScheduledPublications() {
        int publishedCount = publicationAdminService.publishScheduledPublications();

        if (publishedCount > 0) {
            log.info("Published {} scheduled publications", publishedCount);
        }
    }
}
