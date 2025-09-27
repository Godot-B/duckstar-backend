package com.duckstar.service;

import com.duckstar.domain.Anime;
import com.duckstar.domain.mapping.Episode;
import com.duckstar.repository.AnimeOtt.AnimeOttRepository;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.util.ChosungUtil;
import com.duckstar.web.dto.SearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.duckstar.web.dto.SearchResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {
    private final AnimeRepository animeRepository;
    private final AnimeOttRepository animeOttRepository;

    private final AnimeService animeService;

    public SearchResponseDto searchAnimes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return SearchResponseDto.builder()
                    .size(0)
                    .animePreviews(List.of())
                    .build();
        }

        List<Anime> animes = animeRepository.findAll();

        // 데이터 수천건 등 쌓인다면 반드시 애니마다 '초성 칼럼' 두고, 인덱싱 필요
        List<AnimePreviewDto> animePreviews = animes.stream()
                .filter(anime -> ChosungUtil.searchMatch(query, anime.getTitleKor()))
                .map(anime -> {

                    // 에피소드, OTT 등 리포지토리 단에서 최적화 반드시 필요 (N+1 문제)
                    // 현재는 애니 데이터가 몇 개 없으므로 그냥 둔다.
                            // (1)
                            Optional<Episode> episodeOpt = animeService.findCurrentEpisode(anime, LocalDateTime.now());
                            Episode currentEpisode = episodeOpt.orElse(null);

                            return AnimePreviewDto.builder()
                                    .animeId(anime.getId())
                                    .mainThumbnailUrl(anime.getMainThumbnailUrl())
                                    .status(anime.getStatus())
                                    .isBreak(
                                            currentEpisode != null ? currentEpisode.getIsBreak() : null
                                    )
                                    .titleKor(anime.getTitleKor())
                                    .dayOfWeek(anime.getDayOfWeek())
                                    .isRescheduled(
                                            currentEpisode != null ? currentEpisode.getIsRescheduled() : null
                                    )
                                    .scheduledAt(
                                            currentEpisode != null ? currentEpisode.getScheduledAt() : null
                                    )
                                    .airTime(anime.getAirTime())
                                    .genre(anime.getGenre())
                                    .medium(anime.getMedium())
                                    .ottDtos(
                                            // (2)
                                            animeOttRepository.getOttDtosByAnimeId(anime.getId())
                                    )
                                    .build();
                        }
                )
                .toList();

        return SearchResponseDto.builder()
                .size(animePreviews.size())
                .animePreviews(animePreviews)
                .build();
    }
}
