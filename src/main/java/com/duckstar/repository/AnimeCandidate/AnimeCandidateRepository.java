package com.duckstar.repository.AnimeCandidate;

import com.duckstar.domain.mapping.AnimeCandidate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimeCandidateRepository extends JpaRepository<AnimeCandidate, Long>, AnimeCandidateRepositoryCustom {
    List<AnimeCandidate> getAnimeCandidatesByWeekId(Long weekId, Pageable pageable);

    List<AnimeCandidate> findAllByWeek_Id(Long weekId);

    boolean existsByAnime_Id(Long animeId);
}
