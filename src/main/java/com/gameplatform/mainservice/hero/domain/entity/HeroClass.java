package com.gameplatform.mainservice.hero.domain.entity;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.media.migration.SimpleImageEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "hero_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeroClass implements SimpleImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "name_json", nullable = false, columnDefinition = "jsonb")
    private LocalizedTextJson nameJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "base_name_json", columnDefinition = "jsonb")
    private LocalizedTextJson baseNameJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "base_description_json", columnDefinition = "jsonb")
    private LocalizedTextJson baseDescriptionJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "master_name_json", columnDefinition = "jsonb")
    private LocalizedTextJson masterNameJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "master_description_json", columnDefinition = "jsonb")
    private LocalizedTextJson masterDescriptionJson;

    @Column(name = "image_bucket")
    private String imageBucket;

    @Column(name = "image_object_key", length = 1024)
    private String imageObjectKey;
}

