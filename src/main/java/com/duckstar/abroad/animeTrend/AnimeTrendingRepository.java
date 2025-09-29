package com.duckstar.abroad.animeTrend;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimeTrendingRepository extends JpaRepository<AnimeTrending, Long> {
    List<AnimeTrending> findAllByWeek_Id(Long weekId);
    List<AnimeTrending> findAllByWeek_Id(Long weekId, Pageable pageable);
}
