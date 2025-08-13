package com.duckstar.repository.AnimeWeek;

import java.time.LocalDateTime;

import static com.duckstar.web.dto.WeekResponseDto.*;

public interface WeekAnimeRepositoryCustom {
    WeekDataDto getWeekDataByAnimeInfo(Long animeId, LocalDateTime premiereDateTime);


}
