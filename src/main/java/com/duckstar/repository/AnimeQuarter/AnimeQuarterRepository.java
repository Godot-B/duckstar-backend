package com.duckstar.repository.AnimeQuarter;

import com.duckstar.domain.mapping.AnimeQuarter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimeQuarterRepository extends JpaRepository<AnimeQuarter, Long>, AnimeQuarterRepositoryCustom {
}
