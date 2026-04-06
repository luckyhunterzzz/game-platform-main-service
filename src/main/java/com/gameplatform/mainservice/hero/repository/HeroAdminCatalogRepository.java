package com.gameplatform.mainservice.hero.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HeroAdminCatalogRepository {

    Page<Long> findHeroIds(String search, Pageable pageable);
}
