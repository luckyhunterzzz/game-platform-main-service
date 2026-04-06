package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.repository.projection.HeroCardRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HeroCatalogRepository {

    Page<HeroCardRow> findReadyBaseHeroCards(
            String locale,
            String search,
            List<Long> elementIds,
            List<Long> rarityIds,
            List<Long> heroClassIds,
            List<Long> familyIds,
            List<Long> manaSpeedIds,
            List<Long> alphaTalentIds,
            Pageable pageable
    );
}
