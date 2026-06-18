package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.BugReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BugReportRepository extends JpaRepository<BugReport, UUID> {

    boolean existsByHeroIdAndIsOpenTrue(Long heroId);

    Optional<BugReport> findByHeroIdAndIsOpenTrue(Long heroId);
}
