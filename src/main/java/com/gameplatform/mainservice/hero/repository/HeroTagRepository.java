package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.HeroTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HeroTagRepository extends JpaRepository<HeroTag, Long> {

    List<HeroTag> findAllByIdIn(List<Long> ids);
}
