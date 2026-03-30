package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.PassiveSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassiveSkillRepository extends JpaRepository<PassiveSkill, Long> {

    List<PassiveSkill> findAllByIdIn(List<Long> ids);
}