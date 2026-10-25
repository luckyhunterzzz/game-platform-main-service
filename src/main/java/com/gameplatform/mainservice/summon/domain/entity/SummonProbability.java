package com.gameplatform.mainservice.summon.domain.entity;

import com.gameplatform.mainservice.summon.domain.enums.SummonProbabilityKind;
import com.gameplatform.mainservice.summon.domain.enums.SummonHeroVariantMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "summon_probabilities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummonProbability {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "variant_id", nullable = false)
    private UUID variantId;

    @Column(name = "definition_id", nullable = false)
    private UUID definitionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 40)
    private SummonProbabilityKind kind;

    @Column(name = "value_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal valuePercent;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "hero_variant_mode", nullable = false, length = 32)
    private SummonHeroVariantMode heroVariantMode = SummonHeroVariantMode.EXACT;

    @Column(name = "user_selection_enabled", nullable = false)
    private boolean userSelectionEnabled;

    @Column(name = "default_selected_hero_count")
    private Integer defaultSelectedHeroCount;
}
