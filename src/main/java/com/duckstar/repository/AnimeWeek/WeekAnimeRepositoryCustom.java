package com.duckstar.repository.AnimeWeek;

import com.duckstar.web.dto.WeekResponseDto;

import java.time.LocalDateTime;

public interface WeekAnimeRepositoryCustom {
    WeekResponseDto.WeekDataDto getWeekDataByAnimeInfo(Long animeId, LocalDateTime premiereDateTime);
}
