package com.duckstar.repository.AnimeCandidate;

import com.duckstar.web.dto.MedalDto.RackUnitDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.duckstar.web.dto.AnimeResponseDto.*;

public interface AnimeCandidateRepositoryCustom {
    List<AnimeRankDto> getAnimeRankDtosByWeekId(Long weekId, Pageable pageable);
    List<RackUnitDto> getRackUnitsByAnimeId(Long animeId);
}
