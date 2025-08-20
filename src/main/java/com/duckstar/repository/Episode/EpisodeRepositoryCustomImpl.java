package com.duckstar.repository.Episode;

import com.duckstar.domain.QWeek;
import com.duckstar.domain.mapping.Episode;
import com.duckstar.domain.mapping.QEpisode;
import com.duckstar.web.dto.EpisodeDto;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.duckstar.web.dto.EpisodeDto.*;

@Repository
@RequiredArgsConstructor
public class EpisodeRepositoryCustomImpl implements EpisodeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QEpisode episode = QEpisode.episode;
    private final QWeek week = QWeek.week;

    @Override
    public List<EpisodeDto> getEpisodeDtosByAnimeId(Long animeId) {

        // 분기, 주차 util 계산으로 복잡도 낮추기 가능

        List<Tuple> tuples = queryFactory.select(
                        episode.episodeNumber,
                        episode.isBreak,
                        week.quarter.quarterValue,
                        week.weekValue,
                        episode.scheduledAt,
                        episode.isRescheduled,
                        episode.nextEpScheduledAt
                )
                .from(episode)
                .join(week).on(episode.scheduledAt.between(week.startDateTime, week.endDateTime))
                .where(episode.anime.id.eq(animeId))
                .orderBy(episode.scheduledAt.asc())
                .fetch();

        return tuples.stream().map(t ->
                        EpisodeDto.builder()
                                .episodeNumber(t.get(episode.episodeNumber))
                                .isBreak(t.get(episode.isBreak))
                                .quarter(t.get(week.quarter.quarterValue))
                                .week(t.get(week.weekValue))
                                .scheduledAt(t.get(episode.scheduledAt))
                                .isRescheduled(t.get(episode.isRescheduled))
                                .nextEpScheduledAt(t.get(episode.nextEpScheduledAt))
                                .build()
                )
                .toList();
    }
}
