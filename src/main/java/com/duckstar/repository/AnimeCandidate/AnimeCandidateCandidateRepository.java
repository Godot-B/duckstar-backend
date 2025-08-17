package com.duckstar.repository.AnimeCandidate;

import com.duckstar.domain.mapping.AnimeCandidate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimeCandidateCandidateRepository extends JpaRepository<AnimeCandidate, Long>, AnimeCandidateRepositoryCustom {
    List<AnimeCandidate> getWeekAnimesByWeekId(Long weekId, Pageable pageable);
}
