package com.duckstar.repository.AnimeComment;

import com.duckstar.domain.enums.CommentSortType;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.QCommentLike;
import com.duckstar.domain.mapping.QEpisode;
import com.duckstar.domain.mapping.QReply;
import com.duckstar.domain.mapping.comment.QAnimeComment;
import com.duckstar.domain.mapping.comment.QComment;
import com.duckstar.security.MemberPrincipal;
import com.querydsl.core.BooleanBuilder;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.duckstar.web.dto.CommentResponseDto.*;

@Repository
@RequiredArgsConstructor
public class AnimeCommentRepositoryCustomImpl implements AnimeCommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QComment comment = QComment.comment;
    private final QCommentLike commentLike = QCommentLike.commentLike;
    private final QReply reply = QReply.reply;
    private final QEpisode episode = QEpisode.episode;

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
                            commentLike.comment.id.eq(comment.id),
                            commentLike.member.id.eq(principalId)
                    )
                    .limit(1);

            isLikedExpression = JPAExpressions
                    .select(commentLike.isLiked)
                    .from(commentLike)
                    .where(
                            commentLike.comment.id.eq(comment.id),
                            commentLike.member.id.eq(principalId)
                    )
                    .limit(1);
        } else {
            principalId = null;
            isAdmin = false;

            likeIdSubquery = Expressions.nullExpression(Long.class);
            isLikedExpression = Expressions.constant(false);
        }

        BooleanExpression animeCondition = comment.dtype.eq("A").and(
                comment.contentIdForIdx.eq(animeId));

        BooleanExpression episodeCondition = episodeIds.isEmpty() ? null : episode.id.in(episodeIds);

        int pageSize = pageable.getPageSize();
        List<Tuple> tuples = queryFactory.select(
                        likeIdSubquery,
                        isLikedExpression,
                        comment.status,
                        comment.id,
                        comment.likeCount,
                        comment.author.id,
                        comment.author.nickname,
                        comment.author.profileImageUrl,
                        comment.isUserTaggedEp,
                        comment.voteCount,
                        comment.episode.episodeNumber,
                        comment.createdAt,
                        comment.attachedImageUrl,
                        comment.body,
                        comment.replyCount
                )
                .from(comment)
                .leftJoin(comment.episode, episode)
                .where(
                        animeCondition,
                        episodeCondition,
                        comment.status.eq(CommentStatus.NORMAL)
                                .or(comment.replyCount.gt(0))
                )
                .orderBy(getOrder(sortBy))  // 정렬
                .offset((long) pageable.getPageNumber() * (pageSize - 1))
                .limit(pageSize)
                .fetch();

        List<Long> commentIds = tuples.stream()
                .map(t -> t.get(comment.id))
                .toList();
        if (commentIds.isEmpty()) {
            return List.of();
        }

        return tuples.stream()
                .map(t -> {
                    Long commentId = t.get(comment.id);

                    CommentStatus status = t.get(comment.status);
                    LocalDateTime createdAt = t.get(comment.createdAt);

                    Boolean isUserTaggedEp = t.get(comment.isUserTaggedEp);
                    Integer episodeNumber = Boolean.TRUE.equals(isUserTaggedEp) ?
                            t.get(comment.episode.episodeNumber) :
                            null;

                    Integer replyCount = t.get(comment.replyCount);

                    if (status != CommentStatus.NORMAL) {
                        return CommentDto.ofDeleted(
                                status,
                                commentId,
                                createdAt,
                                episodeNumber,
                                replyCount
                        );
                    }

                    Long authorId = t.get(comment.author.id);
                    boolean canDelete = Objects.equals(authorId, principalId) || isAdmin;

                    return CommentDto.builder()
                            .status(status)
                            .commentId(commentId)

                            .canDeleteThis(canDelete)

                            .isLiked(t.get(isLikedExpression))
                            .commentLikeId(t.get(likeIdSubquery))
                            .likeCount(t.get(comment.likeCount))

                            .authorId(authorId)
                            .nickname(t.get(comment.author.nickname))
                            .profileImageUrl(t.get(comment.author.profileImageUrl))
                            .voteCount(t.get(comment.voteCount))
                            .episodeNumber(
                                    episodeNumber
                            )
                            .createdAt(createdAt)
                            .attachedImageUrl(t.get(comment.attachedImageUrl))
                            .body(t.get(comment. body))

                            .replyCount(replyCount)
                            .build();
                })
                .toList();
    }

    @Override
    public Integer countTotalElements(Long animeId, List<Long> episodeIds) {
        BooleanExpression animeCondition = comment.dtype.eq("A")
                .and(comment.contentIdForIdx.eq(animeId));

        BooleanExpression episodeCondition = episodeIds.isEmpty() ? null : episode.id.in(episodeIds);

        Long commentsCount = queryFactory.select(comment.count())
                .from(comment)
                .leftJoin(comment.episode, episode)
                .where(
                        animeCondition,
                        comment.status.eq(CommentStatus.NORMAL),
                        episodeCondition
                )
                .fetchOne();

        Long repliesCount = queryFactory.select(reply.count())
                .from(reply)
                .join(reply.parent, comment)
                .leftJoin(comment.episode, episode)
                .where(
                        animeCondition,
                        reply.status.eq(CommentStatus.NORMAL),
                        episodeCondition
                )
                .fetchOne();

        return Optional.ofNullable(commentsCount).orElse(0L).intValue() +
                Optional.ofNullable(repliesCount).orElse(0L).intValue();
    }

    private OrderSpecifier<?>[] getOrder(CommentSortType sortBy) {
        return switch (sortBy) {
            case POPULAR -> new OrderSpecifier<?>[]{
                    comment.likeCount.desc(),
                    comment.replyCount.desc(),
                    comment.createdAt.desc()
            };
            case RECENT -> new OrderSpecifier<?>[]{comment.createdAt.desc()};
            case OLDEST -> new OrderSpecifier<?>[]{comment.createdAt.asc()};
        };
    }
}
