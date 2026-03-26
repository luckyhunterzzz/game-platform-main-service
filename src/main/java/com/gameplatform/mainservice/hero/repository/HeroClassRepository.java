package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.HeroClass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeroClassRepository extends JpaRepository<HeroClass, Long> {
}