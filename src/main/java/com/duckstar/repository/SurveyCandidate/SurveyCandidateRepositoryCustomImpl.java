package com.duckstar.repository.SurveyCandidate;

import com.duckstar.domain.QAnime;
import com.duckstar.domain.QQuarter;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.QCommentLike;
import com.duckstar.domain.mapping.QReply;
import com.duckstar.domain.mapping.comment.QAnimeComment;
import com.duckstar.domain.mapping.surveyVote.QSurveyCandidate;
import com.duckstar.domain.mapping.weeklyVote.QEpisode;
import com.duckstar.domain.mapping.weeklyVote.QEpisodeStar;
import com.duckstar.repository.AnimeComment.AnimeCommentRepositoryCustomImpl;
import com.duckstar.security.MemberPrincipal;
import com.querydsl.core.Tuple;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.duckstar.web.dto.CommentResponseDto.*;
import static com.duckstar.web.dto.RankInfoDto.*;
import static com.duckstar.web.dto.SurveyResponseDto.*;

@Repository
@RequiredArgsConstructor
public class SurveyCandidateRepositoryCustomImpl implements SurveyCandidateRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QSurveyCandidate surveyCandidate = QSurveyCandidate.surveyCandidate;
    private final QAnime anime = QAnime.anime;
    private final QQuarter quarter = QQuarter.quarter;
    private final QCommentLike commentLike = QCommentLike.commentLike;
    private final QAnimeComment animeComment = QAnimeComment.animeComment;
    private final QEpisode episode = QEpisode.episode;
    private final QEpisodeStar episodeStar = QEpisodeStar.episodeStar;
    private final AnimeCommentRepositoryCustomImpl animeCommentRepositoryCustomImpl;
    private final QReply reply = QReply.reply;

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
                .orderBy(
                        surveyCandidate.quarter.quarterValue.asc(),
                        surveyCandidate.title.asc()
                )
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

    @Override
    public Page<SurveyRankDto> getSurveyRankDtosBySurveyId(
            Long surveyId,
            MemberPrincipal principal,
            Pageable pageable
    ) {
        List<SurveyRankDto> content = queryFactory.select(
                        Projections.constructor(
                                SurveyRankDto.class,
                                surveyCandidate.rank,
                                anime.id,
                                Projections.constructor(
                                        AnimeCandidateDto.class,
                                        surveyCandidate.id,
                                        surveyCandidate.thumbnailUrl,
                                        surveyCandidate.title,
                                        quarter.yearValue,
                                        quarter.quarterValue,
                                        surveyCandidate.medium
                                ),
                                Projections.constructor(
                                        VoteRatioDto.class,
                                        surveyCandidate.votePercent,
                                        surveyCandidate.normalPercent,
                                        surveyCandidate.bonusPercent,
                                        surveyCandidate.malePercent,
                                        surveyCandidate.femalePercent,
                                        surveyCandidate.under14Percent,
                                        surveyCandidate.to19Percent,
                                        surveyCandidate.to24Percent,
                                        surveyCandidate.to29Percent,
                                        surveyCandidate.to34Percent,
                                        surveyCandidate.over35Percent
                                )
                        )
                )
                .from(surveyCandidate)
                .leftJoin(anime).on(anime.id.eq(surveyCandidate.anime.id))
                .where(surveyCandidate.survey.id.eq(surveyId))
                .orderBy(surveyCandidate.rank.asc(), surveyCandidate.title.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // animeId들 추출
        List<Long> animeIds = content.stream()
                .map(SurveyRankDto::getAnimeId)
                .filter(Objects::nonNull)
                .toList();

        if (!animeIds.isEmpty()) {
            Long principalId;
            boolean isAdmin;
            Expression<Long> likeIdSubquery;
            Expression<Boolean> isLikedExpression;

            if (principal != null) {
                principalId = principal.getId();
                isAdmin = principal.isAdmin();

                likeIdSubquery = JPAExpressions
                        .select(commentLike.id)
                        .from(commentLike)
                        .where(
                                commentLike.comment.id.eq(animeComment.id),
                                commentLike.member.id.eq(principalId)
                        )
                        .limit(1);

                isLikedExpression = JPAExpressions
                        .select(commentLike.isLiked)
                        .from(commentLike)
                        .where(
                                commentLike.comment.id.eq(animeComment.id),
                                commentLike.member.id.eq(principalId)
                        )
                        .limit(1);
            } else {
                principalId = null;
                isAdmin = false;

                likeIdSubquery = Expressions.nullExpression(Long.class);
                isLikedExpression = Expressions.constant(false);
            }

            // 1. 댓글 맵 구성
            Map<Long, List<Tuple>> tupleMap = queryFactory.from(animeComment)
                    .leftJoin(episode).on(episode.id.eq(animeComment.episode.id))
                    .leftJoin(episodeStar).on(episodeStar.id.eq(animeComment.episodeStar.id))
                    .where(
                            animeComment.dtype.eq("A").and(
                                    animeComment.anime.id.in(animeIds)),
                            animeComment.status.notIn(CommentStatus.DELETED, CommentStatus.ADMIN_DELETED)
                    )
                    .orderBy(animeComment.createdAt.desc())
                    .transform(GroupBy.groupBy(animeComment.anime.id).as(
                            GroupBy.list(
                                    Projections.tuple(
                                            animeComment.status,
                                            animeComment.id,
                                            isLikedExpression,
                                            likeIdSubquery,
                                            animeComment.likeCount,
                                            animeComment.author.id,
                                            animeComment.author.nickname,
                                            animeComment.author.profileImageUrl,
                                            animeComment.isUserTaggedEp,
                                            animeComment.voteCount,
                                            animeComment.createdAt,
                                            animeComment.attachedImageUrl,
                                            animeComment.body,
                                            animeComment.replyCount,
                                            animeComment.surveyCandidate.id,
                                            episode.episodeNumber,
                                            episodeStar.starScore,
                                            episodeStar.isLateParticipating
                                    )
                            )
                    ));

            // 2. 댓글 수 집계
            Map<Long, Long> commentCounts = queryFactory
                    .from(animeComment)
                    .where(animeComment.anime.id.in(animeIds),
                            animeComment.status.notIn(CommentStatus.DELETED, CommentStatus.ADMIN_DELETED))
                    .groupBy(animeComment.anime.id)
                    .transform(GroupBy.groupBy(animeComment.anime.id).as(animeComment.count()));

            // 3. 답글 수 집계
            Map<Long, Long> replyCounts = queryFactory
                    .from(reply)
                    .join(animeComment).on(animeComment.id.eq(reply.parent.id))
                    .where(animeComment.anime.id.in(animeIds),
                            reply.status.notIn(CommentStatus.DELETED, CommentStatus.ADMIN_DELETED))
                    .groupBy(animeComment.anime.id)
                    .transform(GroupBy.groupBy(animeComment.anime.id).as(reply.count()));

            // 각 DTO 최종 셋팅
            content.forEach(dto -> {
                // 1. List<CommentDto> 셋팅
                Long animeId = dto.getAnimeId();
                List<Tuple> tuples = tupleMap.getOrDefault(animeId, List.of());
                List<CommentDto> commentDtos = tuples.stream()
                        .limit(3)
                        .map(t -> animeCommentRepositoryCustomImpl.toCommentDto(
                                t,
                                principalId,
                                isAdmin,
                                isLikedExpression,
                                likeIdSubquery
                        ))
                        .toList();

                dto.setCommentDtos(commentDtos);

                // 2. 댓글 수 셋팅
                Long cCount = commentCounts.getOrDefault(dto.getAnimeId(), 0L);
                Long rCount = replyCounts.getOrDefault(dto.getAnimeId(), 0L);
                dto.setCommentTotalCount(cCount + rCount);
            });
        }

        // 전체 카운트 쿼리
        Long totalCount = queryFactory
                .select(surveyCandidate.count())
                .from(surveyCandidate)
                .where(surveyCandidate.survey.id.eq(surveyId))
                .fetchOne();

        // 페이지 결과
        return new PageImpl<>(content, pageable, totalCount != null ? totalCount : 0L);
    }
}
