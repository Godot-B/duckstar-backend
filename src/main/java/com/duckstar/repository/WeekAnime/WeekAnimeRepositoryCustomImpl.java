package com.duckstar.repository.WeekAnime;

import com.duckstar.domain.QAnime;
import com.duckstar.domain.QWeek;
import com.duckstar.domain.mapping.QWeekAnime;
import com.duckstar.web.dto.AnimeResponseDto.AnimeRankDto;
import com.duckstar.web.dto.AnimeResponseDto.AnimeStatDto;
import com.duckstar.web.dto.MedalDto.MedalPreviewDto;
import com.duckstar.web.dto.MedalDto.RackUnitDto;
import com.duckstar.web.dto.SummaryDto.RankSummaryDto;
import com.duckstar.web.dto.VoteResponseDto.VoteRatioDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.duckstar.web.dto.WeekResponseDto.*;

@Repository
@RequiredArgsConstructor
public class WeekAnimeRepositoryCustomImpl implements WeekAnimeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QWeekAnime weekAnime = QWeekAnime.weekAnime;
    private final QWeek week = QWeek.week;
    private final QAnime anime = QAnime.anime;

    @Override
    public List<AnimeRankDto> getAnimeRankDtosByWeekId(Long weekId, Pageable pageable) {
        List<Tuple> tuples = queryFactory.select(
                        anime.id,
                        weekAnime.rankInfo.rank,
                        weekAnime.rankInfo.rankDiff,
                        weekAnime.rankInfo.consecutiveWeeksAtSameRank,
                        anime.mainThumbnailUrl,
                        anime.titleKor,
                        anime.corp,
                        anime.debutRank,
                        anime.debutDate,
                        weekAnime.rankInfo.peakRank,
                        weekAnime.rankInfo.peakDate,
                        weekAnime.rankInfo.weeksOnTop10,
                        weekAnime.rankInfo.votePercent,
                        weekAnime.rankInfo.malePercent
                ).from(weekAnime)
                .join(anime).on(anime.id.eq(weekAnime.anime.id))
                .where(weekAnime.week.id.eq(weekId))
                // 추후에 메달 개수 정렬 기준 2순위로 삽입
                .orderBy(weekAnime.rankInfo.rank.asc(), anime.titleKor.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<Long> animeIds = tuples.stream()
                .map(t -> t.get(anime.id))
                .toList();
        if (animeIds.isEmpty()) {
            return List.of();
        }

        // GroupBy 이해 필요
        Map<Long, List<MedalPreviewDto>> medalDtosMap = queryFactory.from(weekAnime)
                .join(week).on(week.id.eq(weekAnime.week.id))
                .where(weekAnime.anime.id.in(animeIds))
                .orderBy(week.startDateTime.asc())
                .transform(GroupBy.groupBy(weekAnime.anime.id).as(
                        GroupBy.list(
                                Projections.constructor(
                                        MedalPreviewDto.class,
                                        weekAnime.rankInfo.type,
                                        weekAnime.rankInfo.rank,
                                        week.quarter.yearValue,
                                        week.quarter.quarterValue,
                                        week.weekValue
                                )
                        )));

        return tuples.stream()
                .map(t -> {
                    Long animeId = t.get(anime.id);

                    RankSummaryDto rankSummaryDto = RankSummaryDto.builder()
                            .rank(t.get(weekAnime.rankInfo.rank))
                            .rankDiff(t.get(weekAnime.rankInfo.rankDiff))
                            .consecutiveWeeksAtSameRank(t.get(weekAnime.rankInfo.consecutiveWeeksAtSameRank))
                            .mainThumbnailUrl(t.get(anime.mainThumbnailUrl))
                            .title(t.get(anime.titleKor))
                            .subTitle(t.get(anime.corp))
                            .build();

                    List<MedalPreviewDto> medalPreviewDtos =
                            medalDtosMap.getOrDefault(animeId, List.of());

                    AnimeStatDto animeStatDto = AnimeStatDto.builder()
                            .debutRank(t.get(anime.debutRank))
                            .debutDate(t.get(anime.debutDate))
                            .peakRank(t.get(weekAnime.rankInfo.peakRank))
                            .peakDate(t.get(weekAnime.rankInfo.peakDate))
                            .weeksOnTop10(t.get(weekAnime.rankInfo.weeksOnTop10))
                            .build();

                    Double malePercent = t.get(weekAnime.rankInfo.malePercent);
                    if (malePercent == null) malePercent = 0.0;
                    VoteRatioDto voteRatioDto = VoteRatioDto.builder()
                            .votePercent(t.get(weekAnime.rankInfo.votePercent))
                            .malePercent(malePercent)
                            .femalePercent(100.0 - malePercent)
                            .build();

                    return AnimeRankDto.builder()
                            .animeId(animeId)
                            .rankSummaryDto(rankSummaryDto)
                            .medalPreviews(medalPreviewDtos)
                            .animeStatDto(animeStatDto)
                            .voteRatioDto(voteRatioDto)
                            .build();
                })
                .toList();
    }

    @Override
    public WeekDataDto getWeekDataByAnimeInfo(Long animeId, LocalDateTime premiereDateTime) {

        DateTemplate<LocalDate> startDateTemplate = Expressions.dateTemplate(
                LocalDate.class, "DATE({0})", week.startDateTime);

        DateTemplate<LocalDate> endDateTemplate = Expressions.dateTemplate(
                LocalDate.class, "DATE({0})", week.endDateTime);

        List<Tuple> tuples = queryFactory.select(
                        weekAnime.isBreak,
                        week.quarter.quarterValue,
                        week.weekValue,
                        weekAnime.airDateTime,
                        startDateTemplate,
                        endDateTemplate,
                        week.quarter.yearValue,
                        weekAnime.rankInfo.type,
                        weekAnime.rankInfo.rank,
                        weekAnime.rankInfo.votePercent,
                        weekAnime.rankInfo.malePercent
                )
                .from(weekAnime)
                .join(week).on(week.id.eq(weekAnime.week.id))
                .where(weekAnime.anime.id.eq(animeId))
                .orderBy(week.startDateTime.asc())
                .fetch();

        // 에피소드 공개
        List<AllKindsWeekDto> allKindsWeekDtos = tuples.stream()
                .map(t -> {
                            LocalDateTime airDateTime = t.get(weekAnime.airDateTime);
                            LocalDateTime airDateTimePlusWeek = (airDateTime == null) ?
                                    null : airDateTime.plusWeeks(1);

                            return AllKindsWeekDto.builder()
                                    .isBreak(t.get(weekAnime.isBreak))
                                    .quarter(t.get(week.quarter.quarterValue))
                                    .week(t.get(week.weekValue))
                                    .airDateTime(airDateTime)
                                    .airDateTimePlusWeek(airDateTimePlusWeek)
                                    .build();
                        }
                )
                .toList();

        // 분기 성적
        List<RackUnitDto> rackUnitDtos = tuples.stream()
                .filter(this::isNotBreak)
                .map(t -> {
                    MedalPreviewDto medalPreviewDto = MedalPreviewDto.builder()
                            .type(t.get(weekAnime.rankInfo.type))
                            .rank(t.get(weekAnime.rankInfo.rank))
                            .year(t.get(week.quarter.yearValue))
                            .quarter(t.get(week.quarter.quarterValue))
                            .week(t.get(week.weekValue))
                            .build();

                    Double malePercent = t.get(weekAnime.rankInfo.malePercent);
                    if (malePercent == null) malePercent = 0.0;

                    VoteRatioDto voteRatioDto = VoteRatioDto.builder()
                            .votePercent(t.get(weekAnime.rankInfo.votePercent))
                            .malePercent(malePercent)
                            .femalePercent(100.0 - malePercent)
                            .build();

                    return RackUnitDto.builder()
                            .startDate(t.get(startDateTemplate))
                            .endDate(t.get(endDateTemplate))
                            .medalPreviewDto(medalPreviewDto)
                            .voteRatioDto(voteRatioDto)
                            .build();
                })
                .toList();

        return WeekDataDto.builder()
                .allKindsWeekDtos(allKindsWeekDtos)
                .rackUnitDtos(rackUnitDtos)
                .build();
    }

    private boolean isNotBreak(Tuple t) {
        Boolean isBreak = t.get(weekAnime.isBreak);
        return !(Boolean.TRUE.equals(isBreak));
    }
}
