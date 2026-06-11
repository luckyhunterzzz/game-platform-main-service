package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.HeroTagGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeroTagGroupRepository extends JpaRepository<HeroTagGroup, Long> {
}
