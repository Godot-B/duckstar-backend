package com.duckstar.repository.Week;

import com.duckstar.domain.Week;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WeekRepository extends JpaRepository<Week, Long>, WeekRepositoryCustom {
    List<Week> findByStartDateTimeLessThanEqualOrderByStartDateTimeDesc(LocalDateTime startDateTimeIsLessThan, PageRequest pageRequest);
}
