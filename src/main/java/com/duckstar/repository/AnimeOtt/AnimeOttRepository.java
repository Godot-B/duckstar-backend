package com.duckstar.repository.AnimeOtt;

import com.duckstar.domain.mapping.AnimeOtt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimeOttRepository extends JpaRepository<AnimeOtt, Long>, AnimeOttRepositoryCustom {
}
