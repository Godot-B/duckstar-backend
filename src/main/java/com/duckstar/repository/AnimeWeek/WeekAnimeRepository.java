package com.duckstar.repository.AnimeWeek;

import com.duckstar.domain.mapping.WeekAnime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeekAnimeRepository extends JpaRepository<WeekAnime, Long>, WeekAnimeRepositoryCustom {
}
