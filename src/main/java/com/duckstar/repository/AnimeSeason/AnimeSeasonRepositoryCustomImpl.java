package com.duckstar.repository.AnimeSeason;

import com.duckstar.domain.*;
import com.duckstar.domain.mapping.QAnimeOtt;
import com.duckstar.domain.mapping.QAnimeSeason;
import com.duckstar.domain.mapping.QWeekAnime;
import com.duckstar.web.dto.AnimeResponseDto;
import com.duckstar.web.dto.AnimeResponseDto.OttDto;
import com.duckstar.web.dto.AnimeResponseDto.SeasonDto;
import com.duckstar.web.dto.SearchResponseDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static com.duckstar.web.dto.SearchResponseDto.*;

@Repository
@RequiredArgsConstructor
public class AnimeSeasonRepositoryCustomImpl implements AnimeSeasonRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QAnimeSeason animeSeason = QAnimeSeason.animeSeason;
    private final QSeason season = QSeason.season;
    private final QAnime anime = QAnime.anime;
    private final QWeekAnime weekAnime = QWeekAnime.weekAnime;
    private final QOtt ott = QOtt.ott;
    private final QAnimeOtt animeOtt = QAnimeOtt.animeOtt;

    @Override
    public List<AnimePreviewDto> getSeasonAnimesByQuarterId(Long quarterId, Long currentWeekId) {
        List<Tuple> tuples = queryFactory.select(
                        anime.id,
                        anime.status,
                        anime.mainThumbnailUrl,
                        weekAnime.isBreak,
                        anime.titleKor,
                        anime.dayOfWeek,
                        anime.airTime,
                        weekAnime.rescheduledAt,
                        anime.genre,
                        anime.medium
                )
                .from(anime)
                .join(animeSeason).on(animeSeason.anime.id.eq(anime.id))
                // 현 주차로 스코프 고정: 없을 수도 있으니 leftJoin
                .leftJoin(weekAnime).on(weekAnime.anime.id.eq(anime.id)
                        .and(weekAnime.week.id.eq(currentWeekId)))
                .where(animeSeason.season.quarter.id.eq(quarterId))
                .orderBy(anime.airTime.asc())
                .fetch();

        List<Long> animeIds = tuples.stream()
                .map(t -> t.get(anime.id))
                .toList();
        if (animeIds.isEmpty()) {
            return List.of();
        }

        Map<Long, List<OttDto>> ottDtosMap = queryFactory.from(animeOtt)
                .join(ott).on(animeOtt.ott.id.eq(ott.id))
                .where(animeOtt.anime.id.in(animeIds))
                .orderBy(ott.typeOrder.asc())
                .transform(GroupBy.groupBy(animeOtt.anime.id).as(
                        GroupBy.list(
                                Projections.constructor(
                                        OttDto.class,
                                        ott.type,
                                        animeOtt.watchUrl
                                )
                        )));

        return tuples.stream()
                .map(t -> {
                    Long animeId = t.get(anime.id);
                    List<OttDto> ottDtos = ottDtosMap.getOrDefault(animeId, List.of());

                    return AnimePreviewDto.builder()
                            .animeId(animeId)
                            .mainThumbnailUrl(t.get(anime.mainThumbnailUrl))
                            .status(t.get(anime.status))
                            .isBreak(t.get(weekAnime.isBreak))
                            .titleKor(t.get(anime.titleKor))
                            .dayOfWeek(t.get(anime.dayOfWeek))
                            .airTime(t.get(anime.airTime))
                            .rescheduledAt(t.get(weekAnime.rescheduledAt))
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
                .join(season).on(season.id.eq(animeSeason.season.id))
                .where(animeSeason.anime.id.eq(animeId))
                .orderBy(season.yearValue.asc(), season.typeOrder.asc())
                .fetch();
    }
}
