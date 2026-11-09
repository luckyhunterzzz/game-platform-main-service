package com.gameplatform.mainservice.supporter.repository;

import com.gameplatform.mainservice.supporter.domain.Supporter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupporterRepository extends JpaRepository<Supporter, Long> {

    List<Supporter> findAllByOrderBySortOrderAscIdAsc();

    List<Supporter> findAllByActiveTrueOrderBySortOrderAscIdAsc();
}
