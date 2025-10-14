package com.duckstar.repository.WeekVoteSubmission;

import com.duckstar.domain.mapping.QEpisodeStar;
import com.duckstar.domain.mapping.QWeekVoteSubmission;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WeekVoteSubmissionRepositoryCustomImpl implements WeekVoteSubmissionRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QEpisodeStar episodeStar = QEpisodeStar.episodeStar;
    private final QWeekVoteSubmission weekVoteSubmission = QWeekVoteSubmission.weekVoteSubmission;

    @Override
    public boolean existsByWeek_IdAndMember_Id(Long weekId, Long memberId) {
        return queryFactory.select(
                        episodeStar.starScore
                ).from(episodeStar)
                .join(episodeStar.weekVoteSubmission, weekVoteSubmission)
                .where(
                        weekVoteSubmission.week.id.eq(weekId)
                                .and(weekVoteSubmission.member.id.eq(memberId))
                                // null 이거나 0인 starScore 제외
                                .and(episodeStar.starScore.gt(0))
                )
                .fetchFirst() != null;
    }
}
