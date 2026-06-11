package com.gameplatform.mainservice.hero.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class HeroTagId implements Serializable {
    @Column(name = "hero_id")
    private Long heroId;

    @Column(name = "tag_id")
    private Long tagId;
}
