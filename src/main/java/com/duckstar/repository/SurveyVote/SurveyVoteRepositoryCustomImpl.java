package com.duckstar.repository.SurveyVote;

import com.duckstar.domain.QAnime;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.comment.QAnimeComment;
import com.duckstar.domain.mapping.surveyVote.QSurveyCandidate;
import com.duckstar.domain.mapping.surveyVote.QSurveyVote;
import com.duckstar.web.dto.SurveyResponseDto;
import com.duckstar.web.dto.VoteResponseDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

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
