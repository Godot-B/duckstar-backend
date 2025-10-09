package com.duckstar.abroad.animeCorner;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AnimeCornerRepositoryCustom {
    List<AnimeCorner> findAllByWeek_IdWithOverFetch(Long weekId, Pageable pageable);
}
