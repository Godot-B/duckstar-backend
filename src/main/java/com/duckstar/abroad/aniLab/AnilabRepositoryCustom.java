package com.duckstar.abroad.aniLab;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AnilabRepositoryCustom {
    List<Anilab> findAllByWeek_IdWithOverFetch(Long weekId, Pageable pageable);
}
