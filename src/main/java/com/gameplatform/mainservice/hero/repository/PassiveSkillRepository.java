package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.PassiveSkill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassiveSkillRepository extends JpaRepository<PassiveSkill, Long> {
}