package com.gameplatform.mainservice.media.migration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.media-migration")
public class MediaMigrationProperties {

    private boolean enabled = false;
    private boolean dryRun = true;
    private int limit = 100;
}
