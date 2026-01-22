package com.duckstar.service.AnimeService;

import com.duckstar.domain.Anime;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.duckstar.web.dto.AnimeResponseDto.*;
import static com.duckstar.web.dto.RankInfoDto.*;
import static com.duckstar.web.dto.admin.ContentResponseDto.*;

public interface AnimeQueryService {
    List<DuckstarRankPreviewDto> getAnimeRankPreviewsByWeekId(Long weekId, int size);

    List<RankPreviewDto> getAnimeCornerPreviewsByWeekId(Long weekId, int size);

    List<RankPreviewDto> getAnilabPreviewsByWeekId(Long weekId, int size);

    List<Long> getAllAnimeIds();

    AnimeHomeDto getAnimeHomeDtoById(Long animeId);

    Optional<Episode> findCurrentEpisode(Anime anime, LocalDateTime now);

    AdminAnimeListDto getAdminAnimeListDto(Long quarterId, Pageable pageable);
}
