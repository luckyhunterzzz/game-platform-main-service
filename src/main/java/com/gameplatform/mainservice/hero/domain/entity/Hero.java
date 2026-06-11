package com.gameplatform.mainservice.hero.domain.entity;

import com.gameplatform.mainservice.hero.domain.enums.HeroStatus;
import com.gameplatform.mainservice.hero.dto.json.CostumeBonusJson;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "heroes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "name_json", nullable = false, columnDefinition = "jsonb")
    private LocalizedTextJson nameJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "special_skill_name_json", nullable = false, columnDefinition = "jsonb")
    private LocalizedTextJson specialSkillNameJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "special_skill_description_json", nullable = false, columnDefinition = "jsonb")
    private LocalizedTextJson specialSkillDescriptionJson;

    @Column(name = "base_attack")
    private Integer baseAttack;

    @Column(name = "base_armor")
    private Integer baseArmor ;

    @Column(name = "base_hp")
    private Integer baseHp;

    @Column(name = "base_power")
    private Integer basePower;

    @Column(name = "element_id", nullable = false)
    private Long elementId;

    @Column(name = "rarity_id", nullable = false)
    private Long rarityId;

    @Column(name = "hero_class_id", nullable = false)
    private Long heroClassId;

    @Column(name = "family_id")
    private Long familyId;

    @Column(name = "mana_speed_id", nullable = false)
    private Long manaSpeedId;

    @Column(name = "alpha_talent_id")
    private Long alphaTalentId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "image_bucket_json", columnDefinition = "jsonb")
    private LocalizedTextJson imageBucketJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "image_object_key_json", columnDefinition = "jsonb")
    private LocalizedTextJson imageObjectKeyJson;

    @Column(name = "preview_bucket")
    private String previewBucket;

    @Column(name = "preview_object_key")
    private String previewObjectKey;

    @Column(name = "is_costume", nullable = false)
    private boolean isCostume;

    @Column(name = "base_hero_id")
    private Long baseHeroId;

    @Column(name = "costume_index")
    private Integer costumeIndex;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "costume_bonus_json", columnDefinition = "jsonb")
    private CostumeBonusJson costumeBonusJson;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private HeroStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @Column(name = "updated_by_email")
    private String updatedByEmail;
}
