package com.duckstar.repository.SurveyCandidate;

import com.duckstar.domain.mapping.surveyVote.QSurveyCandidate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.duckstar.web.dto.SurveyResponseDto.*;

@Repository
@RequiredArgsConstructor
public class SurveyCandidateRepositoryCustomImpl implements SurveyCandidateRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QSurveyCandidate surveyCandidate = QSurveyCandidate.surveyCandidate;

    @Override
    public List<AnimeCandidateDto> getCandidateDtosBySurveyId(Long surveyId) {
        return queryFactory.select(
                        Projections.constructor(
                                AnimeCandidateDto.class,
                                surveyCandidate.id,
                                surveyCandidate.thumbnailUrl,
                                surveyCandidate.title,
                                surveyCandidate.quarter.yearValue,
                                surveyCandidate.quarter.quarterValue,
                                surveyCandidate.medium
                        )
                ).from(surveyCandidate)
                .where(surveyCandidate.survey.id.eq(surveyId))
                .orderBy(surveyCandidate.title.asc())
                .fetch();
    }

    @Override
    public List<Long> findValidIdsForSurvey(Long surveyId, List<Long> candidateIds) {
        if (candidateIds == null || candidateIds.isEmpty()) return List.of();

        return queryFactory.select(surveyCandidate.id)
                .from(surveyCandidate)
                .where(surveyCandidate.survey.id.eq(surveyId),
                        surveyCandidate.id.in(candidateIds))
                .fetch();
    }
}
