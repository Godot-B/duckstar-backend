package com.duckstar.repository.AnimeWeek;

import com.duckstar.domain.QRankInfo;
import com.duckstar.domain.QWeek;
import com.duckstar.domain.mapping.QWeekAnime;
import com.duckstar.web.dto.MedalDto;
import com.duckstar.web.dto.MedalDto.MedalPreviewDto;
import com.duckstar.web.dto.MedalDto.RackUnitDto;
import com.duckstar.web.dto.VoteResponseDto;
import com.duckstar.web.dto.VoteResponseDto.VoteRatioDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static com.duckstar.web.dto.WeekResponseDto.*;

@Repository
@RequiredArgsConstructor
public class WeekAnimeRepositoryCustomImpl implements WeekAnimeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QWeekAnime weekAnime = QWeekAnime.weekAnime;
    private final QWeek week = QWeek.week;

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
