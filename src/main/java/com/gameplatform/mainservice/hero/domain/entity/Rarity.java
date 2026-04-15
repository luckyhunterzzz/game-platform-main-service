package com.gameplatform.mainservice.hero.domain.entity;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "rarities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rarity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "name_json", nullable = false, columnDefinition = "jsonb")
    private LocalizedTextJson nameJson;

    @Column(name = "stars")
    private Integer stars;

    @Column(name = "image_bucket")
    private String imageBucket;

    @Column(name = "image_object_key", length = 1024)
    private String imageObjectKey;
}
