package com.duckstar.service.AnimeService;

import com.duckstar.web.dto.admin.EpisodeRequestDto;

import java.io.IOException;

import static com.duckstar.web.dto.EpisodeResponseDto.*;
import static com.duckstar.web.dto.admin.AnimeRequestDto.*;

public interface AnimeCommandService {
    void updateStatesByWindows();

    Long addAnime(PostRequestDto request) throws IOException;

    Long updateAnimeImage(Long animeId, ImageRequestDto request) throws IOException;

    EpisodeResultDto updateTotalEpisodes(Long animeId, EpisodeRequestDto request);

    EpisodeResultDto setUnknown(Long animeId);

    void breakEpisode(Long animeId, Long episodeId);
}