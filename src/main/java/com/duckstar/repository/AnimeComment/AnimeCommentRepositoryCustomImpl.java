package com.duckstar.repository.AnimeComment;

import com.duckstar.domain.enums.CommentSortType;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.QCommentLike;
import com.duckstar.domain.mapping.QReply;
import com.duckstar.domain.mapping.comment.QAnimeComment;
import com.duckstar.domain.mapping.weeklyVote.QEpisode;
import com.duckstar.domain.mapping.weeklyVote.QEpisodeStar;
import com.duckstar.security.MemberPrincipal;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.duckstar.web.dto.CommentResponseDto.*;

@Repository
@RequiredArgsConstructor
public class AnimeCommentRepositoryCustomImpl implements AnimeCommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QAnimeComment animeComment = QAnimeComment.animeComment;
    private final QCommentLike commentLike = QCommentLike.commentLike;
    private final QReply reply = QReply.reply;
    private final QEpisode episode = QEpisode.episode;
    private final QEpisodeStar episodeStar = QEpisodeStar.episodeStar;

    @Override
    public List<CommentDto> getCommentDtos(
            Long animeId,
            List<Long> episodeIds,
            CommentSortType sortBy,
            Pageable pageable,
            MemberPrincipal principal
    ) {
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

        BooleanExpression animeCondition = animeComment.dtype.eq("A").and(
                animeComment.contentIdForIdx.eq(animeId));

        BooleanExpression episodeCondition = episodeIds.isEmpty() ? null : episode.id.in(episodeIds);

        int pageSize = pageable.getPageSize();
        List<Tuple> tuples = queryFactory.select(
                        likeIdSubquery,
                        isLikedExpression,
                        animeComment.status,
                        animeComment.id,
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
                .from(animeComment)
                .leftJoin(episode).on(episode.id.eq(animeComment.episode.id))
                .leftJoin(episodeStar).on(episodeStar.id.eq(animeComment.episodeStar.id))
                .where(
                        animeCondition,
                        episodeCondition,
                        animeComment.status.notIn(CommentStatus.DELETED, CommentStatus.ADMIN_DELETED)
                                .or(animeComment.replyCount.gt(0))
                )
                .orderBy(getOrder(sortBy))  // 정렬
                .offset((long) pageable.getPageNumber() * (pageSize - 1))
                .limit(pageSize)
                .fetch();

        //=== 이 부분 왜 있지? ===//
        List<Long> commentIds = tuples.stream()
                .map(t -> t.get(animeComment.id))
                .toList();
        if (commentIds.isEmpty()) {
            return List.of();
        }
        //=== 주석 처리하고 테스트 ===//

        return tuples.stream()
                .map(t ->
                        toCommentDto(
                                t,
                                principalId,
                                isAdmin,
                                isLikedExpression,
                                likeIdSubquery
                        ))
                .toList();
    }

    public CommentDto toCommentDto(
            Tuple t,
            Long principalId,
            boolean isAdmin,
            Expression<Boolean> isLikedExpression,
            Expression<Long> likeIdSubquery
    ) {
        Long commentId = t.get(animeComment.id);

        CommentStatus status = t.get(animeComment.status);
        LocalDateTime createdAt = t.get(animeComment.createdAt);

        Boolean isUserTaggedEp = t.get(animeComment.isUserTaggedEp);
        Integer episodeNumber = Boolean.TRUE.equals(isUserTaggedEp) ?
                t.get(episode.episodeNumber) :
                null;

        Integer replyCount = t.get(animeComment.replyCount);

        if (status != CommentStatus.NORMAL) {
            return CommentDto.ofDeleted(
                    status,
                    commentId,
                    createdAt,
                    episodeNumber,
                    replyCount
            );
        }

        Long authorId = t.get(animeComment.author.id);
        boolean canDelete = Objects.equals(authorId, principalId) || isAdmin;

        return CommentDto.builder()
                .status(status)
                .commentId(commentId)

                .canDeleteThis(canDelete)

                .isLiked(t.get(isLikedExpression))
                .commentLikeId(t.get(likeIdSubquery))
                .likeCount(t.get(animeComment.likeCount))

                .authorId(authorId)
                .nickname(t.get(animeComment.author.nickname))
                .profileImageUrl(t.get(animeComment.author.profileImageUrl))
                .voteCount(t.get(animeComment.voteCount))
                .episodeNumber(
                        episodeNumber
                )
                .createdAt(createdAt)
                .attachedImageUrl(t.get(animeComment.attachedImageUrl))
                .body(t.get(animeComment.body))

                .replyCount(replyCount)
                .starScore(t.get(episodeStar.starScore))
                .isLateParticipating(t.get(episodeStar.isLateParticipating))

                .surveyCandidateId(t.get(animeComment.surveyCandidate.id))
                .build();
    }

    @Override
    public Integer countTotalElements(Long animeId, List<Long> episodeIds) {
        BooleanExpression animeCondition = animeComment.dtype.eq("A")
                .and(animeComment.contentIdForIdx.eq(animeId));

        BooleanExpression episodeCondition = episodeIds.isEmpty() ? null : episode.id.in(episodeIds);

        Long commentsCount = queryFactory.select(animeComment.count())
                .from(animeComment)
                .leftJoin(animeComment.episode, episode)
                .where(
                        animeCondition,
                        animeComment.status.notIn(CommentStatus.DELETED, CommentStatus.ADMIN_DELETED),
                        episodeCondition
                )
                .fetchOne();

        Long repliesCount = queryFactory.select(reply.count())
                .from(reply)
                .join(animeComment).on(animeComment.id.eq(reply.parent.id))
                .leftJoin(animeComment.episode, episode)
                .where(
                        animeCondition,
                        reply.status.ne(CommentStatus.DELETED).and(
                                reply.status.ne(CommentStatus.ADMIN_DELETED)
                        ),
                        episodeCondition
                )
                .fetchOne();

        return Optional.ofNullable(commentsCount).orElse(0L).intValue() +
                Optional.ofNullable(repliesCount).orElse(0L).intValue();
    }

    private OrderSpecifier<?>[] getOrder(CommentSortType sortBy) {
        return switch (sortBy) {
            case POPULAR -> new OrderSpecifier<?>[]{
                    animeComment.likeCount.desc(),
                    animeComment.replyCount.desc(),
                    animeComment.createdAt.desc()
            };
            case RECENT -> new OrderSpecifier<?>[]{animeComment.createdAt.desc()};
            case OLDEST -> new OrderSpecifier<?>[]{animeComment.createdAt.asc()};
        };
    }
}
