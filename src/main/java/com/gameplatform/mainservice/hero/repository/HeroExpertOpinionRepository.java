package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.HeroExpertOpinion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HeroExpertOpinionRepository extends JpaRepository<HeroExpertOpinion, Long> {

    @Query(value = """
            SELECT *
            FROM hero_expert_opinions
            WHERE hero_id = :heroId
            ORDER BY published_at DESC NULLS LAST, id DESC
            """, nativeQuery = true)
    List<HeroExpertOpinion> findAllByHeroIdOrdered(@Param("heroId") Long heroId);

    @Query(value = """
            SELECT *
            FROM hero_expert_opinions
            WHERE hero_id = :heroId
              AND is_published = true
            ORDER BY published_at DESC NULLS LAST, id DESC
            """, nativeQuery = true)
    List<HeroExpertOpinion> findPublishedByHeroIdOrdered(@Param("heroId") Long heroId);

    Optional<HeroExpertOpinion> findByIdAndHeroId(Long id, Long heroId);

    void deleteAllByHeroId(Long heroId);
}
