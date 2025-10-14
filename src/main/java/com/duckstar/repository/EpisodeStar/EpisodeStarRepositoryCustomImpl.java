package com.duckstar.repository.EpisodeStar;

import com.duckstar.domain.mapping.Episode;
import com.duckstar.domain.mapping.QEpisode;
import com.duckstar.domain.mapping.QEpisodeStar;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class EpisodeStarRepositoryCustomImpl implements EpisodeStarRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QEpisode episode = QEpisode.episode;
    private final QEpisodeStar episodeStar = QEpisodeStar.episodeStar;

    @Override
    public Map<Episode, Integer> findEpisodeMapBySubmissionId(Long submissionId) {
        return queryFactory.select(
                        episodeStar.starScore,
                        episode
                )
                .from(episodeStar)
                .join(episode).on(episode.id.eq(episodeStar.episode.id))
                .where(episodeStar.weekVoteSubmission.id.eq(submissionId)
                        .and(episodeStar.starScore.gt(0)))
                .stream()
                .collect(Collectors.toMap(
                        t -> t.get(episode),
                        t -> t.get(episodeStar.starScore)
                ));
    }
}
