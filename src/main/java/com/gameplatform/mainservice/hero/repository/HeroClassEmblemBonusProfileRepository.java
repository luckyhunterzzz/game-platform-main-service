package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.HeroClassEmblemBonusProfile;
import com.gameplatform.mainservice.hero.domain.enums.EmblemPathType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HeroClassEmblemBonusProfileRepository
        extends JpaRepository<HeroClassEmblemBonusProfile, Long> {

    Optional<HeroClassEmblemBonusProfile> findByHeroClassIdAndPathType(
            Long heroClassId,
            EmblemPathType pathType
    );
}