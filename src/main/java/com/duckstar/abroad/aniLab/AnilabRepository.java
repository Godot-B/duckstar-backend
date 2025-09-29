package com.duckstar.abroad.aniLab;

import com.duckstar.abroad.animeTrend.AnimeTrending;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnilabRepository extends JpaRepository<Anilab, Long> {
    List<Anilab> findAllByWeek_Id(Long weekId);
    List<Anilab> findAllByWeek_Id(Long weekId, Pageable pageable);
}
