package com.duckstar.repository.AnimeCandidate;

import com.duckstar.domain.QAnime;
import com.duckstar.domain.QWeek;
import com.duckstar.domain.mapping.QAnimeCandidate;
import com.duckstar.domain.mapping.QEpisode;
import com.duckstar.domain.vo.RankInfo;
import com.duckstar.web.dto.AnimeResponseDto.AnimeRankDto;
import com.duckstar.web.dto.AnimeResponseDto.AnimeStatDto;
import com.duckstar.web.dto.MedalDto.MedalPreviewDto;
import com.duckstar.web.dto.MedalDto.RackUnitDto;
import com.duckstar.web.dto.RankInfoDto;
import com.duckstar.web.dto.RankInfoDto.RankPreviewDto;
import com.duckstar.web.dto.RankInfoDto.VoteRatioDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.duckstar.web.dto.VoteResponseDto.*;

@Repository
@RequiredArgsConstructor
public class AnimeCandidateRepositoryCustomImpl implements AnimeCandidateRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QAnimeCandidate animeCandidate = QAnimeCandidate.animeCandidate;
    private final QEpisode episode = QEpisode.episode;
    private final QWeek week = QWeek.week;
    private final QAnime anime = QAnime.anime;

    @Override
    public List<Long> findValidIdsForWeek(Long ballotWeekId, List<Long> candidateIds) {
        if (candidateIds == null || candidateIds.isEmpty()) return List.of();

        return queryFactory.select(animeCandidate.id)
                .from(animeCandidate)
                .where(animeCandidate.week.id.eq(ballotWeekId)
                        .and(animeCandidate.id.in(candidateIds)))
                .fetch();
    }


    /**
     * 이전 분기에서 넘어오고 회차가 얼마 안남은 애니메이션의 경우,
     * - 후보 목록에서 시즌 정보 달라야.
     * AnimeSeason 리스트를, Anime 단위로 캐싱 (OneToMany 리스트 관리) 필요
     */
    @Override
    public List<AnimeCandidateDto> getAnimeCandidateDtosByWeekId(Long weekId) {
        return queryFactory.select(
                        Projections.constructor(
                                AnimeCandidateDto.class,
                                animeCandidate.id,
                                anime.mainThumbnailUrl,
                                anime.titleKor,
                                anime.medium
                        )
                )
                .from(animeCandidate)
                .join(animeCandidate.anime, anime)
                .where(animeCandidate.week.id.eq(weekId))
                .orderBy(anime.titleKor.asc())
                .fetch();
    }

    @Override
    public List<AnimeRankDto> getAnimeRankDtosByWeekId(Long weekId, Pageable pageable) {
        int pageSize = pageable.getPageSize();

        List<Tuple> tuples = queryFactory.select(
                        anime.id,
                        animeCandidate.rankInfo.rank,
                        animeCandidate.rankInfo.rankDiff,
                        animeCandidate.rankInfo.consecutiveWeeksAtSameRank,
                        anime.mainThumbnailUrl,
                        anime.titleKor,
                        anime.corp,
                        anime.debutRank,
                        anime.debutDate,
                        animeCandidate.rankInfo.peakRank,
                        animeCandidate.rankInfo.peakDate,
                        animeCandidate.rankInfo.weeksOnTop10,
                        animeCandidate.rankInfo.votePercent,
                        animeCandidate.rankInfo.malePercent
                ).from(animeCandidate)
                .join(animeCandidate.anime, anime)
                .where(animeCandidate.week.id.eq(weekId))
                // 추후에 메달 개수 정렬 기준 2순위로 삽입
                .orderBy(animeCandidate.rankInfo.rank.asc(), anime.titleKor.asc())
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
        Map<Long, List<MedalPreviewDto>> medalDtosMap = queryFactory.from(animeCandidate)
                .join(animeCandidate.week, week)
                .where(animeCandidate.anime.id.in(animeIds))
                .orderBy(week.startDateTime.asc())
                .transform(GroupBy.groupBy(animeCandidate.anime.id).as(
                        GroupBy.list(
                                Projections.constructor(
                                        MedalPreviewDto.class,
                                        animeCandidate.rankInfo.type,
                                        animeCandidate.rankInfo.rank,
                                        week.quarter.yearValue,
                                        week.quarter.quarterValue,
                                        week.weekValue
                                )
                        )));

        return tuples.stream()
                .map(t -> {
                    Long animeId = t.get(anime.id);

                    RankPreviewDto rankPreviewDto = RankPreviewDto.builder()
                            .rank(t.get(animeCandidate.rankInfo.rank))
                            .rankDiff(t.get(animeCandidate.rankInfo.rankDiff))
                            .consecutiveWeeksAtSameRank(t.get(animeCandidate.rankInfo.consecutiveWeeksAtSameRank))
                            .mainThumbnailUrl(t.get(anime.mainThumbnailUrl))
                            .title(t.get(anime.titleKor))
                            .subTitle(t.get(anime.corp))
                            .build();

                    List<MedalPreviewDto> medalPreviews =
                            medalDtosMap.getOrDefault(animeId, List.of());

                    AnimeStatDto animeStatDto = AnimeStatDto.builder()
                            .debutRank(t.get(anime.debutRank))
                            .debutDate(t.get(anime.debutDate))
                            .peakRank(t.get(animeCandidate.rankInfo.peakRank))
                            .peakDate(t.get(animeCandidate.rankInfo.peakDate))
                            .weeksOnTop10(t.get(animeCandidate.rankInfo.weeksOnTop10))
                            .build();

                    Double malePercent = t.get(animeCandidate.rankInfo.malePercent);
                    if (malePercent == null) malePercent = 0.0;
                    VoteRatioDto voteRatioDto = RankInfoDto.VoteRatioDto.builder()
                            .votePercent(t.get(animeCandidate.rankInfo.votePercent))
                            .malePercent(malePercent)
                            .femalePercent(100.0 - malePercent)
                            .build();

                    return AnimeRankDto.builder()
                            .animeId(animeId)
                            .rankPreviewDto(rankPreviewDto)
                            .medalPreviews(medalPreviews)
                            .animeStatDto(animeStatDto)
                            .voteRatioDto(voteRatioDto)
                            .build();
                })
                .toList();
    }

    @Override
    public List<RackUnitDto> getRackUnitDtosByAnimeId(Long animeId) {

        DateTemplate<Date> startDateTemplate = Expressions.dateTemplate(
                Date.class, "DATE({0})", week.startDateTime);

        DateTemplate<Date> endDateTemplate = Expressions.dateTemplate(
                Date.class, "DATE({0})", week.endDateTime);

        List<Tuple> tuples = queryFactory.select(
                        week.quarter.quarterValue,
                        week.weekValue,
                        startDateTemplate,
                        endDateTemplate,
                        week.quarter.yearValue,
                        animeCandidate.rankInfo.type,
                        animeCandidate.rankInfo.rank,
                        animeCandidate.rankInfo.votePercent,
                        animeCandidate.rankInfo.malePercent
                )
                .from(animeCandidate)
                .join(animeCandidate.week, week)
                .where(animeCandidate.anime.id.eq(animeId))
                .orderBy(week.startDateTime.asc())
                .fetch();

        return tuples.stream()
                .filter(this::isNotBreak)
                .map(t -> {
                    MedalPreviewDto medalPreviewDto = MedalPreviewDto.builder()
                            .type(t.get(animeCandidate.rankInfo.type))
                            .rank(t.get(animeCandidate.rankInfo.rank))
                            .year(t.get(week.quarter.yearValue))
                            .quarter(t.get(week.quarter.quarterValue))
                            .week(t.get(week.weekValue))
                            .build();

                    Double malePercent = t.get(animeCandidate.rankInfo.malePercent);
                    if (malePercent == null) malePercent = 0.0;

                    VoteRatioDto voteRatioDto = VoteRatioDto.builder()
                            .votePercent(t.get(animeCandidate.rankInfo.votePercent))
                            .malePercent(malePercent)
                            .femalePercent(100.0 - malePercent)
                            .build();

                    Date startDateRaw = t.get(startDateTemplate);
                    Date endDateRaw = t.get(endDateTemplate);

                    return RackUnitDto.builder()
                            .startDate(startDateRaw != null ? startDateRaw.toLocalDate() : null)
                            .endDate(endDateRaw != null ? endDateRaw.toLocalDate() : null)
                            .medalPreviewDto(medalPreviewDto)
                            .voteRatioDto(voteRatioDto)
                            .build();
                })
                .toList();
    }

    private boolean isNotBreak(Tuple t) {
        Boolean isBreak = t.get(episode.isBreak);
        return !(Boolean.TRUE.equals(isBreak));
    }
}
