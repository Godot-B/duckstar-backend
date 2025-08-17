package com.duckstar.repository.Episode;

import com.duckstar.domain.mapping.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpisodeRepository extends JpaRepository<Episode, Long>, EpisodeRepositoryCustom {
}
