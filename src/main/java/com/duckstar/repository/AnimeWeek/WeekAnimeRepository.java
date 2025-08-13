package com.duckstar.repository.AnimeWeek;

import com.duckstar.domain.mapping.WeekAnime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeekAnimeRepository extends JpaRepository<WeekAnime, Long>, WeekAnimeRepositoryCustom {
    List<WeekAnime> getWeekAnimesByWeekId(Long weekId, Pageable pageable);
}
