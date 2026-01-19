package com.duckstar.repository.AnimeQuarter;

import com.duckstar.web.dto.AnimeResponseDto.QuarterDto;
import com.duckstar.web.dto.SearchResponseDto.AnimePreviewDto;

import java.util.List;

public interface AnimeQuarterRepositoryCustom {
    List<AnimePreviewDto> getAnimePreviewsByQuarter(Long quarterId);
    List<QuarterDto> getQuarterDtosByAnimeId(Long animeId);
}
