package com.duckstar.repository;

import com.duckstar.domain.Quarter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuarterRepository extends JpaRepository<Quarter, Long> {

    Optional<Quarter> findByYearValueAndQuarterValue(int yearValue, int quarterValue);
}
