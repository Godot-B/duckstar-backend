package com.duckstar.repository;

import com.duckstar.domain.Anime;
import com.duckstar.domain.enums.AnimeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimeRepository extends JpaRepository<Anime, Long> {
    List<Anime> findAllByStatusOrStatus(AnimeStatus status1, AnimeStatus status2);
    List<Anime> findAllByIdGreaterThanEqual(Long idIsGreaterThan);
}
