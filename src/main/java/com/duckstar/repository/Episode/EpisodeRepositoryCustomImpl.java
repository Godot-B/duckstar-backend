package com.duckstar.repository.Episode;

import com.duckstar.domain.QWeek;
import com.duckstar.domain.mapping.QEpisode;
import com.duckstar.web.dto.EpisodeResponseDto;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.duckstar.web.dto.EpisodeResponseDto.*;

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
}
