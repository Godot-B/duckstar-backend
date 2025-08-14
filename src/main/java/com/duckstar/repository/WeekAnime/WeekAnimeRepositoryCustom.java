package com.duckstar.repository.WeekAnime;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static com.duckstar.web.dto.AnimeResponseDto.*;
import static com.duckstar.web.dto.WeekResponseDto.*;

public interface WeekAnimeRepositoryCustom {
    List<AnimeRankDto> getAnimeRankDtosByWeekId(Long weekId, Pageable pageable);
    WeekDataDto getWeekDataByAnimeInfo(Long animeId, LocalDateTime premiereDateTime);
}
