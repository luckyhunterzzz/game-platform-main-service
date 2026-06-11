package com.gameplatform.mainservice.hero.domain.entity;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "hero_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeroTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "name_json", nullable = false, columnDefinition = "jsonb")
    private LocalizedTextJson nameJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "description_json", columnDefinition = "jsonb")
    private LocalizedTextJson descriptionJson;

    @Column(name = "is_active", nullable = false)
    private boolean active;
}
