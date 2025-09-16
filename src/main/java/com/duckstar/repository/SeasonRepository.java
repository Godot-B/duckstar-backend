package com.duckstar.repository;

import com.duckstar.domain.Season;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeasonRepository extends JpaRepository<Season, Long> {
    Optional<Season> findByYearValueAndQuarter_QuarterValue(Integer yearValue, Integer quarterQuarterValue);
}
