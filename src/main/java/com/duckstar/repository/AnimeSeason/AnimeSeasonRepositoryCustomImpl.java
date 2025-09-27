package com.duckstar.repository.AnimeSeason;

import com.duckstar.domain.*;
import com.duckstar.domain.enums.AnimeStatus;
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
import java.time.Month;
import java.time.format.DateTimeFormatter;
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
    private final QOtt ott = QOtt.ott;
    private final QAnimeOtt animeOtt = QAnimeOtt.animeOtt;

    @Override
    public List<AnimePreviewDto> getAnimePreviewsByQuarter(Long quarterId) {
        List<Tuple> tuples = queryFactory.select(
                        anime.id,
                        anime.status,
                        anime.mainThumbnailUrl,
                        anime.titleKor,
                        anime.dayOfWeek,
                        anime.genre,
                        anime.medium,
                        anime.airTime,
                        anime.premiereDateTime
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

                    Medium medium = t.get(anime.medium);
                    LocalDateTime time = t.get(anime.premiereDateTime);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");
                    String formatted = null;
                    if (time != null) {
                        formatted = time.format(formatter);
                    }

                    AnimeStatus status = t.get(anime.status);

                    String airTime = medium == Medium.MOVIE || status == AnimeStatus.UPCOMING ?
                            formatted :  // 영화일 때와 방영 전 TVA: 첫 방영(개봉) 날짜, 예: "8/22"
                            t.get(anime.airTime);  // TVA 일 때는 방영 시간, 예: "00:00"

                    return AnimePreviewDto.builder()
                            .animeId(animeId)
                            .mainThumbnailUrl(t.get(anime.mainThumbnailUrl))
                            .status(status)
                            .titleKor(t.get(anime.titleKor))
                            .dayOfWeek(t.get(anime.dayOfWeek))
                            .airTime(airTime)
                            .genre(t.get(anime.genre))
                            .medium(medium)
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
