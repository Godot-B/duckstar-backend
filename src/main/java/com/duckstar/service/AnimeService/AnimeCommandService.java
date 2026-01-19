package com.duckstar.service.AnimeService;

import java.io.IOException;

import static com.duckstar.web.dto.admin.AnimeRequestDto.*;
import static com.duckstar.web.dto.admin.ContentResponseDto.*;
import static com.duckstar.web.dto.admin.EpisodeRequestDto.*;

public interface AnimeCommandService {
    void updateStatesByWindows();

    Long addAnime(PostRequestDto request) throws IOException;

    Long updateAnimeImage(Long animeId, ImageRequestDto request) throws IOException;

    EpisodeManageResultDto updateTotalEpisodes(
            Long memberId,
            Long animeId,
            TotalEpisodesRequestDto request
    );

    EpisodeManageResultDto setUnknown(Long memberId, Long animeId);
}