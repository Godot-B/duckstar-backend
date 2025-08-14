package com.duckstar.repository.AnimeCharacter;

import com.duckstar.web.dto.AnimeResponseDto;

import java.util.List;

public interface AnimeCharacterRepositoryCustom {
    List<AnimeResponseDto.CastPreviewDto> getAllCharacterHomePreviewsByAnimeId(Long animeId);
}
