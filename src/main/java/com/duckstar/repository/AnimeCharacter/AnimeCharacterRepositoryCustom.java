package com.duckstar.repository.AnimeCharacter;

import java.util.List;

import static com.duckstar.web.dto.CharacterResponseDto.*;

public interface AnimeCharacterRepositoryCustom {
    List<CharacterHomePreviewDto> getAllCharacterHomePreviewsByAnimeId(Long animeId);
}
