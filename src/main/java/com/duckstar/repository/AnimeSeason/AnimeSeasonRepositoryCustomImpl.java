package com.duckstar.repository.AnimeSeason;

import com.duckstar.domain.*;
import com.duckstar.domain.enums.Medium;
import com.duckstar.domain.mapping.QAnimeOtt;
import com.duckstar.domain.mapping.QAnimeSeason;
import com.duckstar.domain.mapping.QEpisode;
import com.duckstar.web.dto.AnimeResponseDto.OttDto;
import com.duckstar.web.dto.AnimeResponseDto.SeasonDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.duckstar.web.dto.SearchResponseDto.*;

@Repository
@RequiredArgsConstructor
public class AnimeSeasonRepositoryCustomImpl implements AnimeSeasonRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QAnimeSeason animeSeason = QAnimeSeason.animeSeason;
    private final QSeason season = QSeason.season;
    private final QAnime anime = QAnime.anime;
    private final QEpisode episode = QEpisode.episode;
    private final QOtt ott = QOtt.ott;
    private final QAnimeOtt animeOtt = QAnimeOtt.animeOtt;

    @Override
    public List<AnimePreviewDto> getSeasonAnimePreviewsByQuarterAndWeek(
            Long quarterId,
            LocalDateTime weekStart,
            LocalDateTime weekEnd
    ) {
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
                .from(animeSeason)
                .join(animeSeason.anime, anime)
                // 현재 주차 한정: episode null 가능 = leftJoin
                .leftJoin(episode).on(episode.anime.id.eq(anime.id)
                        .and(episode.scheduledAt.between(weekStart, weekEnd)))
                .where(animeSeason.season.quarter.id.eq(quarterId))
                .orderBy(
                        new CaseBuilder()
                                .when(episode.scheduledAt.isNull()).then(1)
                                .otherwise(0)
                                .asc(),
                        episode.scheduledAt.asc()
                )
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

                    Medium medium = t.get(anime.medium);
                    return AnimePreviewDto.builder()
                            .animeId(animeId)
                            .mainThumbnailUrl(t.get(anime.mainThumbnailUrl))
                            .status(t.get(anime.status))
                            .isBreak(t.get(episode.isBreak))
                            .titleKor(t.get(anime.titleKor))
                            .dayOfWeek(t.get(anime.dayOfWeek))
                            .scheduledAt(
                                    medium == Medium.MOVIE ?
                                            t.get(anime.premiereDateTime) :
                                            t.get(episode.scheduledAt)
                            )
                            .isRescheduled(t.get(episode.isRescheduled))
                            .genre(t.get(anime.genre))
                            .medium(medium)
                            .ottDtos(ottDtos)
                            .build();
                })
                .toList();
    }

    @Override
    public List<AnimePreviewDto> getSeasonAnimePreviewsByQuarter(Long quarterId) {
        List<Tuple> tuples = queryFactory.select(
                        anime.id,
                        anime.status,
                        anime.mainThumbnailUrl,
                        anime.titleKor,
                        anime.dayOfWeek,
                        anime.genre,
                        anime.medium,
                        anime.airTime
                )
                .from(animeSeason)
                .join(animeSeason.anime, anime)
                .where(animeSeason.season.quarter.id.eq(quarterId))
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
                            .titleKor(t.get(anime.titleKor))
                            .dayOfWeek(t.get(anime.dayOfWeek))
                            .scheduledAt(null) // 종영 애니메이션의 경우 scheduledAt은 null로 설정
                            .airTime(t.get(anime.airTime)) // 방영시간 추가
                            .genre(t.get(anime.genre))
                            .medium(t.get(anime.medium))
                            .ottDtos(ottDtos)
                            .build();
                })
                .toList();
    }

    @Override
    public List<SeasonDto> getSeasonDtosByAnimeId(Long animeId) {
        return queryFactory.select(
                        Projections.constructor(
                                SeasonDto.class,
                                season.yearValue,
                                season.type
                        )
                )
                .from(animeSeason)
                .join(animeSeason.season, season)
                .where(animeSeason.anime.id.eq(animeId))
                .orderBy(season.yearValue.asc(), season.typeOrder.asc())
                .fetch();
    }
}
