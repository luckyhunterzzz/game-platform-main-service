package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.HeroTagId;
import com.gameplatform.mainservice.hero.domain.entity.HeroTagLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HeroTagLinkRepository extends JpaRepository<HeroTagLink, HeroTagId> {

    List<HeroTagLink> findAllByIdHeroId(Long heroId);

    List<HeroTagLink> findAllByIdHeroIdIn(List<Long> heroIds);

    void deleteAllByIdHeroId(Long heroId);
}
