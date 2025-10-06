package com.duckstar.repository.AnimeCandidate;

import com.duckstar.domain.mapping.AnimeCandidate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnimeCandidateRepository extends JpaRepository<AnimeCandidate, Long>, AnimeCandidateRepositoryCustom {
    @Query("""
    SELECT ac\s
    FROM AnimeCandidate ac
    JOIN ac.anime a
    WHERE ac.week.id = :weekId
    ORDER BY ac.rankInfo.rank ASC, ac.voterCount DESC, a.titleKor ASC
   \s""")
    List<AnimeCandidate> findCandidatesByWeekOrdered(@Param("weekId") Long weekId, Pageable pageable);

    List<AnimeCandidate> findAllByWeek_Id(Long weekId);

    @Query("SELECT ac FROM AnimeCandidate ac WHERE ac.week.id = :weekId ORDER BY function('RAND')")
    List<AnimeCandidate> findAllRandomByWeekId(@Param("weekId") Long weekId, Pageable pageable);

    boolean existsByAnime_Id(Long animeId);

    Optional<AnimeCandidate> findByAnime_IdAndWeek_Id(Long animeId, Long weekId);
}
