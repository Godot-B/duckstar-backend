package com.duckstar.repository.Episode;

import com.duckstar.domain.QAnime;
import com.duckstar.domain.QOtt;
import com.duckstar.domain.QWeek;
import com.duckstar.domain.enums.AnimeStatus;
import com.duckstar.domain.enums.ContentType;
import com.duckstar.domain.enums.Medium;
import com.duckstar.domain.mapping.Episode;
import com.duckstar.domain.mapping.QAnimeOtt;
import com.duckstar.domain.mapping.QEpisode;
import com.duckstar.domain.vo.RankInfo;
import com.duckstar.util.QuarterUtil;
import com.duckstar.web.dto.MedalDto;
import com.duckstar.web.dto.OttDto;
import com.duckstar.web.dto.RankInfoDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.duckstar.web.dto.AnimeResponseDto.*;
import static com.duckstar.web.dto.EpisodeResponseDto.*;
import static com.duckstar.web.dto.MedalDto.*;
import static com.duckstar.web.dto.RankInfoDto.*;
import static com.duckstar.web.dto.SearchResponseDto.*;
import static com.duckstar.web.dto.VoteResponseDto.*;

@Repository
@RequiredArgsConstructor
public class EpisodeRepositoryCustomImpl implements EpisodeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QEpisode episode = QEpisode.episode;
    private final QAnime anime = QAnime.anime;
    private final QAnimeOtt animeOtt = QAnimeOtt.animeOtt;
    private final QOtt ott = QOtt.ott;
    private final QWeek week = QWeek.week;

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
    public List<StarCandidateDto> getStarCandidatesByDuration(LocalDateTime weekStart, LocalDateTime weekEnd) {
        List<Tuple> tuples = queryFactory.select(
                        anime.id,
                        anime.status,
                        anime.mainThumbnailUrl,
                        anime.titleKor,
                        anime.dayOfWeek,
                        anime.genre,
                        anime.medium,
                        anime.premiereDateTime,
                        episode.id,
                        episode.isBreak,
                        episode.isRescheduled,
                        episode.scheduledAt
                )
                .from(anime)
                .leftJoin(episode).on(episode.anime.id.eq(anime.id))
                .where(episode.scheduledAt.between(weekStart, weekEnd)
                                .and(episode.isVoteEnabled)

                        // 극장판은 일단 보류

                        /*.or(
                                anime.status.eq(AnimeStatus.NOW_SHOWING)
                                        .and(anime.medium.eq(Medium.MOVIE))
                        )*/)
                .orderBy(episode.scheduledAt.desc().nullsLast())
                .fetch();

        return tuples.stream()
                .map(t -> {
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

                    LocalDateTime scheduledAt = t.get(episode.scheduledAt);
                    QuarterUtil.YQWRecord record = null;
                    if (scheduledAt != null) {
                        record = QuarterUtil.getThisWeekRecord(scheduledAt);
                    }

                    return StarCandidateDto.builder()
                            .year(record != null ? record.yearValue() : null)
                            .quarter(record != null ? record.quarterValue() : null)
                            .week(record != null ? record.weekValue() : null)
                            .episodeId(t.get(episode.id))
                            .animeId(t.get(anime.id))
                            .mainThumbnailUrl(t.get(anime.mainThumbnailUrl))
                            .status(status)
                            .isBreak(t.get(episode.isBreak))
                            .titleKor(t.get(anime.titleKor))
                            .dayOfWeek(t.get(anime.dayOfWeek))
                            .scheduledAt(scheduledAt)
                            .isRescheduled(t.get(episode.isRescheduled))
                            .airTime(airTime)
                            .genre(t.get(anime.genre))
                            .medium(medium)
                            .build();
                })
                .toList();
    }

    @Override
    public List<AnimePreviewDto> getAnimePreviewsByDuration(LocalDateTime weekStart, LocalDateTime weekEnd) {
        List<Tuple> tuples = queryFactory.select(
                        anime.id,
                        anime.status,
                        anime.mainThumbnailUrl,
                        anime.titleKor,
                        anime.dayOfWeek,
                        anime.genre,
                        anime.medium,
                        anime.premiereDateTime,
                        episode.id,
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
                            .isBreak(t.get(episode.isBreak))
                            .titleKor(t.get(anime.titleKor))
                            .dayOfWeek(t.get(anime.dayOfWeek))
                            .scheduledAt(t.get(episode.scheduledAt))
                            .isRescheduled(t.get(episode.isRescheduled))
                            .airTime(airTime)
                            .genre(t.get(anime.genre))
                            .medium(medium)
                            .ottDtos(ottDtos)
                            .build();
                })
                .toList();
    }

    @Override
    public List<AnimeRankDto> getAnimeRankDtosByWeekIdWithOverFetch(Long weekId, Pageable pageable) {
        int pageSize = pageable.getPageSize();

        List<Tuple> tuples = queryFactory.select(
                        anime.id,
                        anime.mainThumbnailUrl,
                        anime.titleKor,
                        anime.corp,
                        anime.debutRank,
                        anime.debutDate,
                        episode
                ).from(episode)
                .join(episode.anime, anime)
                .where(episode.week.id.eq(weekId))
                .orderBy(episode.rankInfo.rank.asc(), anime.titleKor.asc())
                .offset((long) pageable.getPageNumber() * (pageSize - 1))
                .limit(pageSize)
                .fetch();

        List<Long> animeIds = tuples.stream()
                .map(t -> t.get(anime.id))
                .toList();
        if (animeIds.isEmpty()) {
            return List.of();
        }

        // transform, GroupBy 이해 필요
        Map<Long, List<MedalPreviewDto>> medalDtosMap = queryFactory.from(episode)
                // 랭크 생성 시 Episode 에 주차 관계 셋팅했으므로
                .join(episode.week, week)
                .where(episode.anime.id.in(animeIds))
                .orderBy(week.startDateTime.asc())
                .transform(GroupBy.groupBy(episode.anime.id).as(
                        GroupBy.list(
                                Projections.constructor(
                                        MedalPreviewDto.class,
                                        episode.rankInfo.type,
                                        episode.rankInfo.rank,
                                        week.quarter.yearValue,
                                        week.quarter.quarterValue,
                                        week.weekValue
                                )
                        )));

        return tuples.stream()
                .map(t -> {
                    Long animeId = t.get(anime.id);
                    Episode episodeEntity = t.get(episode);
                    RankInfo rankInfo = episodeEntity != null ? episodeEntity.getRankInfo() : null;

                    RankPreviewDto rankPreviewDto = RankPreviewDto.of(episodeEntity);

                    List<MedalPreviewDto> medalPreviews =
                            medalDtosMap.getOrDefault(animeId, List.of());

                    AnimeStatDto animeStatDto = AnimeStatDto.builder()
                            .debutRank(t.get(anime.debutRank))
                            .debutDate(t.get(anime.debutDate))
                            .peakRank(rankInfo != null ? rankInfo.getPeakRank() : null)
                            .peakDate(rankInfo != null ? rankInfo.getPeakDate() : null)
                            .weeksOnTop10(rankInfo != null ? rankInfo.getWeeksOnTop10() : null)
                            .build();

                    StarInfoDto starInfoDto = StarInfoDto.of(null, episodeEntity);

                    return AnimeRankDto.builder()
                            .rankPreviewDto(rankPreviewDto)
                            .medalPreviews(medalPreviews)
                            .animeStatDto(animeStatDto)
                            .starInfoDto(starInfoDto)
                            .build();
                })
                .toList();
    }
}
