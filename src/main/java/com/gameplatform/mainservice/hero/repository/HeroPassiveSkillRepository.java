package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.HeroPassiveSkill;
import com.gameplatform.mainservice.hero.domain.entity.HeroPassiveSkillId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HeroPassiveSkillRepository extends JpaRepository<HeroPassiveSkill, HeroPassiveSkillId> {

    List<HeroPassiveSkill> findAllByIdHeroId(Long heroId);

    List<HeroPassiveSkill> findAllByIdHeroIdIn(List<Long> heroIds);

    void deleteAllByIdHeroId(Long heroId);
}
