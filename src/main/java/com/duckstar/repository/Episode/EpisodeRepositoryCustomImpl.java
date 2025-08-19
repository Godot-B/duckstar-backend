package com.duckstar.repository.Episode;

import com.duckstar.domain.QWeek;
import com.duckstar.domain.mapping.QEpisode;
import com.duckstar.web.dto.WeekResponseDto.EpisodeDto;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
                        week.quarter.quarterValue,
                        week.weekValue,
                        episode.scheduledAt,
                        episode.isRescheduled
                )
                .from(episode)
                .join(week).on(week.id.eq(episode.week.id))
                .where(episode.anime.id.eq(animeId))
                .orderBy(episode.scheduledAt.asc())
                .fetch();

        List<EpisodeDto> episodeDtos = new ArrayList<>();
        for (int i = 0; i < tuples.size(); i++) {
            Tuple current = tuples.get(i);
            Tuple next = (i + 1) < tuples.size() ? tuples.get(i + 1) : null;

            EpisodeDto episodeDto = EpisodeDto.builder()
                    .episodeNumber(current.get(episode.episodeNumber))
                    .isBreak(current.get(episode.isBreak))
                    .quarter(current.get(week.quarter.quarterValue))
                    .week(current.get(week.weekValue))
                    .scheduledAt(current.get(episode.scheduledAt))
                    .isRescheduled(current.get(episode.isRescheduled))
                    .nextEpScheduledAt(
                            next != null ? next.get(episode.scheduledAt) : null
                    )
                    .build();

            episodeDtos.add(episodeDto);
        }

        return episodeDtos;
    }
}
