package com.duckstar.repository.removeNeeded;

import com.duckstar.domain.mapping.removeNeeded.AnimeSeason;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimeSeasonRepository extends JpaRepository<AnimeSeason, Long> {
}
