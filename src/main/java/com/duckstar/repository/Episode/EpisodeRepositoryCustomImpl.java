package com.duckstar.repository.Episode;

import com.duckstar.domain.QWeek;
import com.duckstar.domain.mapping.QEpisode;
import com.duckstar.web.dto.WeekResponseDto;
import com.duckstar.web.dto.WeekResponseDto.EpisodeDto;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EpisodeRepositoryCustomImpl implements EpisodeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QEpisode episode = QEpisode.episode;
    private final QWeek week = QWeek.week;

    @Override
    public List<EpisodeDto> getEpisodeDtosByAnimeId(Long animeId) {

        List<Tuple> tuples = queryFactory.select(
                        episode.episodeNumber,
                        episode.isBreak,
                        episode.airDateTime,
                        week.quarter.quarterValue,
                        week.weekValue,
                        week.quarter.yearValue
                )
                .from(episode)
                .join(week).on(week.id.eq(episode.week.id))
                .where(episode.anime.id.eq(animeId))
                .orderBy(episode.airDateTime.asc())
                .fetch();

        return tuples.stream()
                .map(t -> {
                            LocalDateTime airDateTime = t.get(episode.airDateTime);
                            LocalDateTime airDateTimePlusWeek = (airDateTime == null) ?
                                    null : airDateTime.plusWeeks(1);

                            return EpisodeDto.builder()
                                    .episodeNumber(t.get(episode.episodeNumber))
                                    .isBreak(t.get(episode.isBreak))
                                    .quarter(t.get(week.quarter.quarterValue))
                                    .week(t.get(week.weekValue))
                                    .airDateTime(airDateTime)
                                    .airDateTimePlusWeek(airDateTimePlusWeek)
                                    .build();
                        }
                )
                .toList();
    }
}
