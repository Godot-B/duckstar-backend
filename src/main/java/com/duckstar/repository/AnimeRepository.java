package com.duckstar.repository;

import com.duckstar.domain.Anime;
import com.duckstar.domain.enums.AnimeStatus;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AnimeRepository extends JpaRepository<Anime, Long> {
    List<Anime> findAllByStatus(AnimeStatus status);
    List<Anime> findAllByStatusOrStatus(AnimeStatus status1, AnimeStatus status2);
    List<Anime> findAllByIdGreaterThanEqual(Long idIsGreaterThan);
    List<Anime> findAllByPremiereDateTimeGreaterThanEqualAndPremiereDateTimeLessThan(LocalDateTime time1, LocalDateTime time2);

    List<Anime> findAllByIdIn(Collection<Long> ids);
}
