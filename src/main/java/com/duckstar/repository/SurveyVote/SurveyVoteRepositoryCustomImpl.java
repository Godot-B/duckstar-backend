package com.duckstar.repository.SurveyVote;

import com.duckstar.domain.QAnime;
import com.duckstar.domain.mapping.surveyVote.QSurveyCandidate;
import com.duckstar.domain.mapping.surveyVote.QSurveyVote;
import com.duckstar.web.dto.SurveyResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.duckstar.web.dto.SurveyResponseDto.*;

@Repository
@RequiredArgsConstructor
public class SurveyVoteRepositoryCustomImpl implements SurveyVoteRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QSurveyVote surveyVote = QSurveyVote.surveyVote;
    private final QSurveyCandidate surveyCandidate = QSurveyCandidate.surveyCandidate;
    private final QAnime anime = QAnime.anime;

    @Override
    public List<AnimeBallotDto> getVoteHistoryBySubmissionId(Long submissionId) {
        return queryFactory.select(
                        Projections.constructor(
                                AnimeBallotDto.class,
                                surveyVote.ballotType,
                                surveyCandidate.id,
                                anime.id,
                                surveyCandidate.thumbnailUrl,
                                surveyCandidate.title,
                                anime.totalEpisodes,
                                anime.medium
                        )
                ).from(surveyVote)
                .join(surveyVote.surveyCandidate, surveyCandidate)
                .leftJoin(surveyCandidate.anime, anime)
                .where(surveyVote.surveyVoteSubmission.id.eq(submissionId))
                .orderBy(surveyVote.score.desc(), surveyCandidate.title.asc())
                .fetch();
    }
}
