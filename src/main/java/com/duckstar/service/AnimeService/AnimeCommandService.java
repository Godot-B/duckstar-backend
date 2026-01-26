package com.duckstar.service.AnimeService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static com.duckstar.web.dto.admin.AnimeRequestDto.*;
import static com.duckstar.web.dto.admin.AdminLogDto.*;
import static com.duckstar.web.dto.admin.ContentResponseDto.*;

public interface AnimeCommandService {
    void updateStatesByWindows();

    Long createAnime(Long memberId, PostRequestDto request) throws IOException;

    Long updateAnimeImage(Long animeId, ImageRequestDto request) throws IOException;

    EpisodeManageResultDto updateTotalEpisodes(
            Long memberId,
            Long animeId,
            TotalEpisodesRequestDto request
    );

    EpisodeManageResultDto setUnknown(Long memberId, Long animeId);

    List<ManagerProfileDto> updateInfo(Long memberId, Long animeId, InfoRequestDto request, LocalDateTime now);
}