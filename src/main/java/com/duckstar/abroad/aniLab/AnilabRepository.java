package com.duckstar.abroad.aniLab;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnilabRepository extends JpaRepository<Anilab, Long>, AnilabRepositoryCustom {
    List<Anilab> findAllByWeek_Id(Long weekId);
    List<Anilab> findAllByWeek_Id(Long weekId, Pageable pageable);
}
