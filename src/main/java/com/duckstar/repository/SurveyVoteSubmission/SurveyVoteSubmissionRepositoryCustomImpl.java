package com.duckstar.repository.SurveyVoteSubmission;

import com.duckstar.domain.mapping.surveyVote.QSurveyVoteSubmission;
import com.duckstar.domain.mapping.surveyVote.SurveyVoteSubmission;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SurveyVoteSubmissionRepositoryCustomImpl implements SurveyVoteSubmissionRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QSurveyVoteSubmission surveyVoteSubmission = QSurveyVoteSubmission.surveyVoteSubmission;

    @Override
    public Optional<SurveyVoteSubmission> findLocalSubmission(Long surveyId, String cookieId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(surveyVoteSubmission)
                        .where(
                                surveyVoteSubmission.survey.id.eq(surveyId)
                                        .and(surveyVoteSubmission.cookieId.eq(cookieId))
                                        .and(surveyVoteSubmission.member.isNull())
                        )
                        .fetchOne());
    }
}
