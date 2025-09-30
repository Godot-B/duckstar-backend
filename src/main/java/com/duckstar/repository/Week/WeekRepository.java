package com.duckstar.repository.Week;

import com.duckstar.domain.Week;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WeekRepository extends JpaRepository<Week, Long>, WeekRepositoryCustom {
    List<Week> findByStartDateTimeLessThanEqualOrderByStartDateTimeDesc(LocalDateTime now, PageRequest pageRequest);

    Optional<Week> findWeekByStartDateTimeLessThanEqualAndEndDateTimeGreaterThan(LocalDateTime now1, LocalDateTime now2);

    Optional<Week> findWeekById(Long id);

    Week findFirstByOrderByStartDateTimeDesc();

    Week findThirdByOrderByStartDateTimeDesc();
}
