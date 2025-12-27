package com.duckstar.repository.Episode;

import com.duckstar.domain.QAnime;
import com.duckstar.domain.QOtt;
import com.duckstar.domain.QWeek;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.AnimeStatus;
import com.duckstar.domain.enums.EpEvaluateState;
import com.duckstar.domain.enums.Medium;
import com.duckstar.domain.mapping.*;
import com.duckstar.domain.mapping.comment.QAnimeComment;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import com.duckstar.domain.mapping.weeklyVote.EpisodeStar;
import com.duckstar.domain.mapping.weeklyVote.QEpisode;
import com.duckstar.domain.mapping.weeklyVote.QEpisodeStar;
import com.duckstar.domain.vo.RankInfo;
import com.duckstar.util.QuarterUtil;
import com.duckstar.web.dto.OttDto;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static com.duckstar.service.AnimeService.AnimeCommandServiceImpl.*;
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
    private final QEpisodeStar episodeStar = QEpisodeStar.episodeStar;
    private final QAnimeComment animeComment = QAnimeComment.animeComment;

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
    public List<LiveCandidateDto> getLiveCandidateDtos(List<String> principalKeys) {
        List<Tuple> tuples = queryFactory.select(
                        anime.id,
                        anime.status,
                        anime.mainThumbnailUrl,
                        anime.titleKor,
                        anime.dayOfWeek,
                        anime.genre,
                        anime.medium,
                        anime.premiereDateTime,
                        anime.airTime,
                        episode,
                        episodeStar,
                        episodeStar.weekVoteSubmission.isBlocked
                )
                .from(episode)
                .join(episode.anime, anime)
                .leftJoin(episodeStar).on(
                        episodeStar.episode.id.eq(episode.id),
                        episodeStar.weekVoteSubmission.principalKey.in(principalKeys),
                        episodeStar.starScore.isNotNull()
                )
                .where(episode.evaluateState.eq(EpEvaluateState.VOTING_WINDOW)

                        // 극장판은 일단 보류

                        /*.or(
                                anime.status.eq(AnimeStatus.NOW_SHOWING)
                                        .and(anime.medium.eq(Medium.MOVIE))
                        )*/)
                .orderBy(episode.scheduledAt.asc())
                .fetch();

        return tuples.stream()
                .map(t -> {
                    Episode episode = t.get(this.episode);
                    if (episode == null) {
                        return null;
                    }

                    Medium medium = t.get(anime.medium);
                    LocalDateTime time = t.get(anime.premiereDateTime);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");
                    String formatted = null;
                    if (time != null) {
                        formatted = time.format(formatter);
                    }
                    AnimeStatus status = t.get(anime.status);

                    String airTime = (medium == Medium.MOVIE || status == AnimeStatus.UPCOMING) ?
                            formatted :  // 영화일 때와 방영 전 TVA: 첫 방영(개봉) 날짜, 예: "8/22"
                            t.get(anime.airTime);  // TVA 일 때는 방영 시간, 예: "00:00"

                    //=== 에피소드가 속한 주 계산 ===//
                    LocalDateTime scheduledAt = episode.getScheduledAt();
                    QuarterUtil.YQWRecord record = scheduledAt != null ?
                            QuarterUtil.getThisWeekRecord(scheduledAt) :
                            null;

                    // EpisodeStar 존재 시 별점 통계 셋팅
                    StarInfoDto info = null;
                    EpisodeStar episodeStar = t.get(this.episodeStar);
                    if (episodeStar != null) {
                        Boolean isBlocked = t.get(this.episodeStar.weekVoteSubmission.isBlocked);
                        info = StarInfoDto.of(isBlocked, episodeStar, episode);
                    }

                    // VoteResultDto 구성
                    VoteResultDto result = VoteResultDto.builder()
                            .voterCount(episode.getVoterCount())
                            .info(info)
                            .build();

                    // 최종 DTO 리턴
                    return LiveCandidateDto.builder()
                            .year(record != null ? record.yearValue() : null)
                            .quarter(record != null ? record.quarterValue() : null)
                            .week(record != null ? record.weekValue() : null)
                            .episodeId(episode.getId())
                            .animeId(t.get(anime.id))
                            .mainThumbnailUrl(t.get(anime.mainThumbnailUrl))
                            .titleKor(t.get(anime.titleKor))
                            .dayOfWeek(t.get(anime.dayOfWeek))
                            .scheduledAt(scheduledAt)
                            .airTime(airTime)
                            .genre(t.get(anime.genre))
                            .medium(medium)
                            .result(result)
                            .build();
                })
                .toList();
    }

    @Override
    public List<WeekCandidateDto> getWeekCandidateDtos(Long weekId, String principalKey) {
        principalKey = principalKey == null ? "" : principalKey;

        return queryFactory.select(
                        Projections.constructor(
                                WeekCandidateDto.class,
                                episode.id,
                                episode.evaluateState,
                                episodeStar.starScore.isNotNull(),
                                anime.mainThumbnailUrl,
                                anime.titleKor
                        )
                )
                .from(episode)
                .join(episode.anime, anime)
                .join(week)
                    .on(week.id.eq(weekId))
                .leftJoin(episodeStar)
                    .on(
                            episodeStar.episode.id.eq(episode.id),
                            episodeStar.weekVoteSubmission.principalKey.eq(principalKey)
                    )
                .where(
                        episode.scheduledAt.between(week.startDateTime, week.endDateTime)
                )
                .orderBy(episode.scheduledAt.asc())
                .fetch();
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
                    String formatted = null;
                    if (time != null) {
                        formatted = time.toString();
                    }
                    AnimeStatus status = t.get(anime.status);

                    String airTime = medium == Medium.MOVIE || status == AnimeStatus.UPCOMING ?
                            formatted :  // 영화일 때와 방영 전 TVA: 첫 방영 dateTime
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
    public List<AnimeRankDto> getAnimeRankDtosByWeekIdWithOverFetch(
            Long weekId,
            LocalDateTime weekEndDateTime,
            Pageable pageable
    ) {
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
                .orderBy(episode.rankInfo.rank.asc(),
                        episode.rankInfo.rankedVoterCount.desc(),
                        episode.rankInfo.rankedAverage.desc(),
                        anime.titleKor.asc())
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
                .where(episode.anime.id.in(animeIds).and(week.announcePrepared)
                        .and(week.startDateTime.before(weekEndDateTime)))
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
                    Episode episode = t.get(this.episode);
                    RankInfo rankInfo = episode != null ? episode.getRankInfo() : null;

                    RankPreviewDto rankPreviewDto = RankPreviewDto.of(episode);

                    List<MedalPreviewDto> medalPreviews =
                            medalDtosMap.getOrDefault(animeId, List.of());

                    AnimeStatDto animeStatDto = AnimeStatDto.builder()
                            .debutRank(t.get(anime.debutRank))
                            .debutDate(t.get(anime.debutDate))
                            .peakRank(rankInfo != null ? rankInfo.getPeakRank() : null)
                            .peakDate(rankInfo != null ? rankInfo.getPeakDate() : null)
                            .weeksOnTop10(rankInfo != null ? rankInfo.getWeeksOnTop10() : null)
                            .build();

                    StarInfoDto starInfoDto = StarInfoDto.of(
                            null,
                            null,
                            episode
                    );

                    VoteResultDto result = VoteResultDto.builder()
                            .voterCount(episode != null ? episode.getVoterCount() : null)
                            .info(starInfoDto)
                            .build();

                    return AnimeRankDto.builder()
                            .rankPreviewDto(rankPreviewDto)
                            .medalPreviews(medalPreviews)
                            .animeStatDto(animeStatDto)
                            .voteResultDto(result)
                            .build();
                })
                .toList();
    }

    @Override
    public List<PremieredEpRecord> findPremieredEpRecordsInWindow(
            LocalDateTime windowStart, LocalDateTime windowEnd) {
        // 방영 종료 체크: scheduledAt + 24분이 윈도우 안에 있는 경우
        // 즉, scheduledAt이 (windowStart - 24분) ~ (windowEnd - 24분) 범위에 있어야 함
        LocalDateTime finishedEpWindowStart = windowStart.minusMinutes(24);
        LocalDateTime finishedEpWindowEnd = windowEnd.minusMinutes(24);

        // 실시간 투표 종료 체크 : scheduledAt + 36시간
        LocalDateTime liveVoteFinishedEpWindowStart = windowStart.minusHours(36);
        LocalDateTime liveVoteFinishedEpWindowEnd = windowEnd.minusHours(36);

        return queryFactory.select(
                        Projections.constructor(
                                PremieredEpRecord.class,
                                episode,
                                episode.scheduledAt.eq(anime.premiereDateTime),  // 첫 번째 에피소드인지
                                episode.isLastEpisode,  // 마지막 에피소드인지
                                episode.scheduledAt.between(
                                        finishedEpWindowStart, finishedEpWindowEnd),
                                episode.scheduledAt.between(
                                        liveVoteFinishedEpWindowStart, liveVoteFinishedEpWindowEnd),
                                anime
                        )
                )
                .from(episode)
                .join(episode.anime, anime)
                .where(
                        episode.isBreak.isFalse(),
                        // 방영 시작 체크: scheduledAt이 윈도우 안에 있음
                        episode.scheduledAt.between(windowStart, windowEnd)
                                // 방영 종료 체크: scheduledAt + 24분이 윈도우 안에 있음
                                .or(episode.scheduledAt.between(
                                        finishedEpWindowStart, finishedEpWindowEnd))
                                // 실시간 투표 종료 체크: scheduledAt + 36시간이 윈도우 안에 있음
                                .or(episode.scheduledAt.between(
                                        liveVoteFinishedEpWindowStart, liveVoteFinishedEpWindowEnd))
                )
                .fetch();
    }

    @Override
    public Optional<CandidateFormDto> getCandidateFormDto(Long episodeId, List<String> principalKeys) {
        Tuple t = queryFactory.select(
                        episode,
                        episodeStar,
                        episodeStar.weekVoteSubmission.isBlocked,
                        anime.id,
                        anime.mainThumbnailUrl,
                        animeComment.id,
                        animeComment.body
                )
                .from(episode)
                .join(episode.anime, anime)
                .leftJoin(episodeStar).on(
                        episodeStar.episode.id.eq(episode.id),
                        episodeStar.weekVoteSubmission.principalKey.in(principalKeys),
                        episodeStar.starScore.isNotNull()
                )
                .leftJoin(animeComment).on(animeComment.episodeStar.id.eq(episodeStar.id))
                .where(episode.id.eq(episodeId))
                .fetchOne();

        if (t == null) {
            return Optional.empty();
        }

        // 에피소드 기본 정보
        Episode episode = t.get(this.episode);
        if (episode == null) {
            return Optional.empty();
        }
        Integer voterCount = episode.getVoterCount();

        // EpisodeStar 존재 시 별점 통계 등 셋팅
        EpisodeStar episodeStar = t.get(this.episodeStar);

        LocalDateTime voteUpdatedAt = null;
        StarInfoDto info = null;
        Boolean isLateParticipating = null;
        if (episodeStar != null) {
            voteUpdatedAt = episodeStar.getUpdatedAt();

            info = StarInfoDto.of(
                    t.get(this.episodeStar.weekVoteSubmission.isBlocked),
                    episodeStar,
                    episode
            );

            isLateParticipating = episodeStar.getIsLateParticipating();
        }

        // VoteFormResultDto 구성
        VoteFormResultDto result = VoteFormResultDto.builder()
                .isLateParticipating(isLateParticipating)
                .voterCount(voterCount)
                .info(info)
                .voteUpdatedAt(voteUpdatedAt)
                .commentId(t.get(animeComment.id))
                .body(t.get(animeComment.body))
                .build();

        // 최종 DTO 리턴
        return Optional.of(
                CandidateFormDto.builder()
                .episodeId(episodeId)
                .voterCount(voterCount)
                .animeId(t.get(anime.id))
                .mainThumbnailUrl(t.get(anime.mainThumbnailUrl))
                .result(result)
                .build()
        );
    }
}
