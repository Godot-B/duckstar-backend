package com.duckstar.abroad.animeCorner;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimeCornerRepository extends JpaRepository<AnimeCorner, Long>, AnimeCornerRepositoryCustom {
    List<AnimeCorner> findAllByWeek_Id(Long weekId);
    List<AnimeCorner> findAllByWeek_Id(Long weekId, Pageable pageable);
}
