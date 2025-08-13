package com.duckstar.repository.AnimeSeason;

import com.duckstar.domain.mapping.AnimeSeason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimeSeasonRepository extends JpaRepository<AnimeSeason, Long>, AnimeSeasonRepositoryCustom {
}
