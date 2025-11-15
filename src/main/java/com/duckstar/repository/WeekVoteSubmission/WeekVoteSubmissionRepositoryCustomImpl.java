package com.duckstar.repository.WeekVoteSubmission;

import com.duckstar.domain.QWeek;
import com.duckstar.domain.mapping.QEpisodeStar;
import com.duckstar.domain.mapping.QWeekVoteSubmission;
import com.duckstar.domain.mapping.WeekVoteSubmission;
import com.duckstar.security.domain.QShadowBan;
import com.duckstar.web.dto.admin.SubmissionResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.duckstar.web.dto.admin.SubmissionResponseDto.*;

@Repository
@RequiredArgsConstructor
public class WeekVoteSubmissionRepositoryCustomImpl implements WeekVoteSubmissionRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QEpisodeStar episodeStar = QEpisodeStar.episodeStar;
    private final QWeekVoteSubmission weekVoteSubmission = QWeekVoteSubmission.weekVoteSubmission;
    private final QWeek week = QWeek.week;
    private final QShadowBan shadowBan = QShadowBan.shadowBan;

    @Override
    public Optional<WeekVoteSubmission> findLocalSubmission(Long weekId, String cookieId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(weekVoteSubmission)
                        .where(
                                weekVoteSubmission.week.id.eq(weekId)
                                        .and(weekVoteSubmission.cookieId.eq(cookieId))
                                        .and(weekVoteSubmission.member.isNull())
                        )
                        .fetchOne());
    }

    @Override
    public List<SubmissionCountDto> getSubmissionCountDtos(Pageable pageable) {
        int pageSize = pageable.getPageSize();

        return queryFactory.select(
                        Projections.constructor(
                                SubmissionCountDto.class,
                                week.id,
                                week.quarter.yearValue,
                                week.quarter.quarterValue,
                                week.weekValue,
                                weekVoteSubmission.ipHash,
                                weekVoteSubmission.count(),
                                shadowBan.banned.max(),
                                shadowBan.isAllWithdrawn.max(),
                                weekVoteSubmission.createdAt.min(),
                                weekVoteSubmission.createdAt.max()
                        )
                ).from(weekVoteSubmission)
                .join(weekVoteSubmission.week, week)
                .leftJoin(shadowBan).on(shadowBan.ipHash.eq(weekVoteSubmission.ipHash))
                .groupBy(week.id, weekVoteSubmission.ipHash)
                .orderBy(week.startDateTime.desc(), weekVoteSubmission.count().desc())
                .offset((long) pageable.getPageNumber() * (pageSize - 1))
                .limit(pageSize)
                .fetch();
    }

    @Override
    public List<EpisodeStarDto> getEpisodeStarDtosByWeekIdAndIpHash(Long weekId, String ipHash) {
        return queryFactory.select(
                        Projections.constructor(
                                EpisodeStarDto.class,
                                episodeStar.episode.anime.titleKor,
                                episodeStar.starScore,
                                episodeStar.weekVoteSubmission.isBlocked,
                                episodeStar.createdAt,
                                episodeStar.updatedAt
                        )
                ).from(episodeStar)
                .where(episodeStar.weekVoteSubmission.week.id.eq(weekId)
                        .and(episodeStar.weekVoteSubmission.ipHash.eq(ipHash))
                )
                .orderBy(episodeStar.updatedAt.desc())
                .fetch();
    }

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
