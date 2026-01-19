package com.duckstar.repository.EpisodeStar;

import com.duckstar.domain.mapping.weeklyVote.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EpisodeStarRepositoryCustomImpl implements EpisodeStarRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QEpisode episode = QEpisode.episode;
    private final QEpisodeStar episodeStar = QEpisodeStar.episodeStar;
    private final QWeekVoteSubmission weekVoteSubmission = QWeekVoteSubmission.weekVoteSubmission;

    @Override
    public List<EpisodeStar> findAllEligibleByWeekId(Long weekId) {
        return queryFactory.selectFrom(episodeStar)
                .join(episodeStar.weekVoteSubmission, weekVoteSubmission)
                .where(
                        weekVoteSubmission.week.id.eq(weekId)
                                .and(episodeStar.starScore.isNotNull())
                                .and(weekVoteSubmission.isBlocked.isFalse())
                )
                .fetch();
    }

    @Override
    public Long getVoteTimeLeftForLatestEpVoted(Long submissionId) {
        LocalDateTime latestEpScheduledAt = queryFactory.select(
                        episode.scheduledAt.max()
                )
                .from(episodeStar)
                .join(episodeStar.episode, episode)
                .where(episodeStar.weekVoteSubmission.id.eq(submissionId))
                .fetchFirst();

        if (latestEpScheduledAt == null) return 0L;

        LocalDateTime voteClosedAt = latestEpScheduledAt.plusHours(36);
        if (LocalDateTime.now().isAfter(voteClosedAt)) return 0L;

        return Duration.between(LocalDateTime.now(), voteClosedAt).getSeconds();
    }
}
