package com.gameplatform.mainservice.hero.domain.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "heroes_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeroTagLink {
    @EmbeddedId
    private HeroTagId id;
}
