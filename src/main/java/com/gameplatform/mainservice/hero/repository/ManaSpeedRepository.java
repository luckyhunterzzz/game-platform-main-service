package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.ManaSpeed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManaSpeedRepository extends JpaRepository<ManaSpeed, Long> {
}