package com.duckstar.repository.Episode;

import com.duckstar.domain.QAnime;
import com.duckstar.domain.QOtt;
import com.duckstar.domain.enums.AnimeStatus;
import com.duckstar.domain.enums.Medium;
import com.duckstar.domain.mapping.QAnimeOtt;
import com.duckstar.domain.mapping.QEpisode;
import com.duckstar.web.dto.AnimeResponseDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.duckstar.web.dto.AnimeResponseDto.*;
import static com.duckstar.web.dto.EpisodeResponseDto.*;
import static com.duckstar.web.dto.SearchResponseDto.*;

@Repository
@RequiredArgsConstructor
public class EpisodeRepositoryCustomImpl implements EpisodeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QEpisode episode = QEpisode.episode;
    private final QAnime anime = QAnime.anime;
    private final QAnimeOtt animeOtt = QAnimeOtt.animeOtt;
    private final QOtt ott = QOtt.ott;

    @Override
    public List<EpisodeDto> getEpisodeDtosByAnimeId(Long animeId) {

        // 분기, 주차 util 계산으로 복잡도 낮추기 가능

        List<Tuple> tuples = queryFactory.select(
                        episode.id,
                        episode.episodeNumber,
                        episode.isBreak,
                        episode.scheduledAt,
                        episode.isRescheduled,
                        episode.nextEpScheduledAt
                )
                .from(episode)
                .where(episode.anime.id.eq(animeId))
                .orderBy(episode.scheduledAt.asc())
                .fetch();

        return tuples.stream().map(t ->
                        EpisodeDto.builder()
                                .episodeId(t.get(episode.id))
                                .episodeNumber(t.get(episode.episodeNumber))
                                .isBreak(t.get(episode.isBreak))
                                .scheduledAt(t.get(episode.scheduledAt))
                                .isRescheduled(t.get(episode.isRescheduled))
                                .nextEpScheduledAt(t.get(episode.nextEpScheduledAt))
                                .build()
                )
                .toList();
    }

    @Override
    public List<AnimePreviewDto> getAnimePreviewsByWeek(LocalDateTime weekStart, LocalDateTime weekEnd) {
        List<Tuple> tuples = queryFactory.select(
                        anime.id,
                        anime.status,
                        anime.mainThumbnailUrl,
                        anime.titleKor,
                        anime.dayOfWeek,
                        anime.genre,
                        anime.medium,
                        anime.premiereDateTime,
                        episode.isBreak,
                        episode.isRescheduled,
                        episode.scheduledAt
                )
                .from(anime)
                .leftJoin(episode).on(episode.anime.id.eq(anime.id))
                .where(episode.scheduledAt.between(weekStart, weekEnd)
                        .or(
                                anime.status.eq(AnimeStatus.NOW_SHOWING)
                                        .and(anime.medium.eq(Medium.MOVIE))
                        ))
                .orderBy(episode.scheduledAt.asc().nullsLast())
                .fetch();

        List<Long> animeIds = tuples.stream()
                .map(t -> t.get(anime.id))
                .toList();
        if (animeIds.isEmpty()) {
            return List.of();
        }

        List<Tuple> animeOttTuples = queryFactory
                .select(
                        animeOtt.anime.id,
                        ott.type,
                        animeOtt.watchUrl
                )
                .from(animeOtt)
                .join(animeOtt.ott, ott)
                .where(animeOtt.anime.id.in(animeIds))
                .orderBy(ott.typeOrder.asc())
                .fetch();

        Map<Long, List<OttDto>> ottDtosMap = animeOttTuples.stream()
                .collect(Collectors.groupingBy(
                        t -> t.get(animeOtt.anime.id),
                        Collectors.mapping(
                                t -> new OttDto(t.get(ott.type), t.get(animeOtt.watchUrl)),
                                Collectors.toList()
                        )
                ));

        return tuples.stream()
                .map(t -> {
                    Long animeId = t.get(anime.id);
                    List<OttDto> ottDtos = ottDtosMap.getOrDefault(animeId, List.of());

                    return AnimePreviewDto.builder()
                            .animeId(animeId)
                            .mainThumbnailUrl(t.get(anime.mainThumbnailUrl))
                            .status(t.get(anime.status))
                            .isBreak(t.get(episode.isBreak))
                            .titleKor(t.get(anime.titleKor))
                            .dayOfWeek(t.get(anime.dayOfWeek))
                            .scheduledAt(t.get(episode.scheduledAt))
                            .isRescheduled(t.get(episode.isRescheduled))
                            .genre(t.get(anime.genre))
                            .medium(t.get(anime.medium))
                            .ottDtos(ottDtos)
                            .build();
                })
                .toList();
    }
}
