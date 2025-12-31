package com.duckstar.repository.SurveyVote;

import com.duckstar.domain.QAnime;
import com.duckstar.domain.enums.AgeGroup;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.enums.Gender;
import com.duckstar.domain.mapping.comment.QAnimeComment;
import com.duckstar.domain.mapping.surveyVote.QSurveyCandidate;
import com.duckstar.domain.mapping.surveyVote.QSurveyVote;
import com.duckstar.domain.mapping.surveyVote.QSurveyVoteSubmission;
import com.duckstar.domain.mapping.surveyVote.SurveyVote;
import com.duckstar.service.ChartService;
import com.duckstar.web.dto.SurveyResponseDto;
import com.duckstar.web.dto.VoteResponseDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static com.duckstar.service.ChartService.*;
import static com.duckstar.web.dto.SurveyResponseDto.*;
import static com.duckstar.web.dto.VoteResponseDto.*;

@Repository
@RequiredArgsConstructor
public class SurveyVoteRepositoryCustomImpl implements SurveyVoteRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QSurveyVote surveyVote = QSurveyVote.surveyVote;
    private final QSurveyCandidate surveyCandidate = QSurveyCandidate.surveyCandidate;
    private final QAnime anime = QAnime.anime;
    private final QAnimeComment animeComment = QAnimeComment.animeComment;
    private final QSurveyVoteSubmission surveyVoteSubmission = QSurveyVoteSubmission.surveyVoteSubmission;

    // 인덱스 점검
    @Override
    public Map<Long, SurveyStatRecord> getEligibleStatMapBySurveyId(Long surveyId, List<String> outlaws) {
        // 동적 조건 생성
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(surveyVoteSubmission.survey.id.eq(surveyId));

        if (outlaws != null && !outlaws.isEmpty()) {
            builder.and(surveyVoteSubmission.ipHash.notIn(outlaws));
        }

        return queryFactory.from(surveyVote)
                .join(surveyVote.surveyVoteSubmission, surveyVoteSubmission)
                .where(builder)
                .groupBy(surveyVote.surveyCandidate.id)
                .transform(GroupBy.groupBy(surveyVote.surveyCandidate.id).as(
                        Projections.constructor(
                                SurveyStatRecord.class,
                                // 도장 점수 합
                                surveyVote.score.sum(),
                                // 투표자 수
                                surveyVoteSubmission.count(),

                                // 1. 표 종류 카운트
                                countIf(surveyVote.score.eq(100)),
                                countIf(surveyVote.score.eq(50)),

                                // 2. 성별 카운트
                                countIf(surveyVoteSubmission.gender.eq(Gender.MALE)),
                                countIf(surveyVoteSubmission.gender.eq(Gender.FEMALE)),

                                // 3. 연령대별 카운트
                                countIf(surveyVoteSubmission.ageGroup.eq(AgeGroup.UNDER_14)),
                                countIf(surveyVoteSubmission.ageGroup.eq(AgeGroup.AGE_15_19)),
                                countIf(surveyVoteSubmission.ageGroup.eq(AgeGroup.AGE_20_24)),
                                countIf(surveyVoteSubmission.ageGroup.eq(AgeGroup.AGE_25_29)),
                                countIf(surveyVoteSubmission.ageGroup.eq(AgeGroup.AGE_30_34)),
                                countIf(surveyVoteSubmission.ageGroup.eq(AgeGroup.OVER_35))
                        )
                ));
    }

    private NumberExpression<Integer> countIf(BooleanExpression condition) {
        // Hibernate 6 대응: 템플릿을 사용하여 명시적으로 Integer 타입의 SUM임을 선언
        return Expressions.numberTemplate(Integer.class,
                "sum(case when {0} then 1 else 0 end)", condition);
    }

    @Override
    public List<AnimeBallotDto> getVoteHistoryBySubmissionId(Long submissionId) {
        List<Tuple> tuples = queryFactory.select(
                        surveyCandidate.id,
                        surveyCandidate.thumbnailUrl,
                        surveyCandidate.title,
                        surveyCandidate.quarter.yearValue,
                        surveyCandidate.quarter.quarterValue,
                        anime.id,
                        anime.totalEpisodes,
                        surveyCandidate.medium,
                        surveyVote.ballotType,
                        animeComment.createdAt,
                        animeComment.id,
                        animeComment.body
                ).from(surveyVote)
                .join(surveyVote.surveyCandidate, surveyCandidate)
                .leftJoin(surveyCandidate.anime, anime)
                .leftJoin(animeComment)
                .on(
                        surveyCandidate.id.eq(animeComment.surveyCandidate.id),
                        animeComment.status.notIn(CommentStatus.DELETED, CommentStatus.ADMIN_DELETED)
                )
                .where(surveyVote.surveyVoteSubmission.id.eq(submissionId))
                .orderBy(
                        surveyVote.score.desc(),
                        surveyCandidate.quarter.quarterValue.asc(),
                        surveyCandidate.title.asc()
                )
                .fetch();

        return tuples.stream()
                .map(t -> {
                    SurveyCommentDto surveyCommentDto = SurveyCommentDto.builder()
                            .commentCreatedAt(t.get(animeComment.createdAt))
                            .commentId(t.get(animeComment.id))
                            .body(t.get(animeComment.body))
                            .build();

                    return AnimeBallotDto.builder()
                            .ballotType(t.get(surveyVote.ballotType))
                            .animeCandidateId(t.get(surveyCandidate.id))
                            .animeId(t.get(anime.id))
                            .mainThumbnailUrl(t.get(surveyCandidate.thumbnailUrl))
                            .titleKor(t.get(surveyCandidate.title))
                            .totalEpisodes(t.get(anime.totalEpisodes))
                            .year(t.get(surveyCandidate.quarter.yearValue))
                            .quarter(t.get(surveyCandidate.quarter.quarterValue))
                            .medium(t.get(surveyCandidate.medium))
                            .surveyCommentDto(surveyCommentDto)
                            .build();
                })
                .toList();
    }
}
