package com.gameplatform.mainservice.publication.domain.entity;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.publication.domain.enums.PublicationStatus;
import com.gameplatform.mainservice.publication.domain.enums.PublicationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "publications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Publication {

    @Id
    private UUID id;

    @Version
    private Integer version;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private PublicationType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "title_json", nullable = false, columnDefinition = "jsonb")
    private LocalizedTextJson titleJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_json", columnDefinition = "jsonb")
    private LocalizedTextJson contentJson;

    @Column(name = "image_bucket")
    private String imageBucket;

    @Column(name = "image_object_key", length = 1024)
    private String imageObjectKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PublicationStatus status;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "is_pinned", nullable = false)
    private boolean pinned;

    @Column(name = "pinned_at")
    private OffsetDateTime pinnedAt;

    @Column(name = "pinned_until")
    private OffsetDateTime pinnedUntil;

    @Column(name = "show_in_news_feed", nullable = false)
    private boolean showInNewsFeed;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column (name = "updated_by")
    private UUID updatedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
