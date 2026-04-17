package com.gameplatform.mainservice.hero.domain.entity;

import com.gameplatform.mainservice.hero.domain.enums.HeroExpertOpinionSourceType;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "hero_expert_opinions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeroExpertOpinion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hero_id", nullable = false)
    private Long heroId;

    @Column(name = "author_name", nullable = false, length = 120)
    private String authorName;

    @Column(name = "source_url", length = 2048)
    private String sourceUrl;

    @Column(name = "source_title", length = 255)
    private String sourceTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 50)
    private HeroExpertOpinionSourceType sourceType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_json", nullable = false, columnDefinition = "jsonb")
    private LocalizedTextJson contentJson;

    @Column(name = "is_published", nullable = false)
    private boolean isPublished;

    @Column(name = "published_at")
    private LocalDate publishedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
