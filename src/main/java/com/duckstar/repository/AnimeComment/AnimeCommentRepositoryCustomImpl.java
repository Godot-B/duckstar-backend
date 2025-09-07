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
    private final QAnimeComment animeComment = QAnimeComment.animeComment;
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
        QCommentLike likeCountAlias = new QCommentLike("likeCountAlias");

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
                    .selectOne()
                    .from(commentLike)
                    .where(
                            commentLike.comment.id.eq(animeComment.id),
                            commentLike.member.id.eq(principalId)
                    )
                    .exists();
        } else {
            principalId = null;
            isAdmin = false;

            likeIdSubquery = Expressions.nullExpression();
            isLikedExpression = Expressions.constant(false);
        }

        List<Tuple> tuples = queryFactory.select(
                        animeComment.status,
                        animeComment.id,
                        isLikedExpression,
                        likeIdSubquery,
                        likeCountAlias.count(),
                        animeComment.author.id,
                        animeComment.author.nickname,
                        animeComment.author.profileImageUrl,
                        animeComment.voteCount,
                        animeComment.episode.episodeNumber,
                        animeComment.createdAt,
                        animeComment.attachedImageUrl,
                        animeComment.body
                )
                .from(animeComment)
                .join(animeComment.episode, episode)
                .leftJoin(likeCountAlias).on(likeCountAlias.comment.id.eq(animeComment.id))
                .where(animeComment.anime.id.eq(animeId),
                        episode.id.in(episodeIds),
                        // 방영 주간
                        animeComment.createdAt.goe(episode.scheduledAt)
                                .and(animeComment.createdAt.lt(episode.nextEpScheduledAt))
                        // 에피소드 댓글
                        .or(animeComment.episode.id.eq(episode.id))
                )
                .groupBy(
                        animeComment.id,
                        animeComment.status,
                        animeComment.author.id,
                        animeComment.author.nickname,
                        animeComment.author.profileImageUrl,
                        animeComment.voteCount,
                        animeComment.episode.episodeNumber,
                        animeComment.createdAt,
                        animeComment.attachedImageUrl,
                        animeComment.body
                )  // MySQL 엄격한 그룹화 규칙
                .orderBy(getOrder(likeCountAlias, sortBy))  // 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<Long> commentIds = tuples.stream()
                .map(t -> t.get(animeComment.id))
                .toList();
        if (commentIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Integer> replyCountMap = queryFactory
                .select(reply.parent.id, reply.count())
                .from(reply)
                .where(reply.parent.id.in(commentIds))
                .groupBy(reply.parent.id)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(reply.parent.id),
                        tuple -> Optional.ofNullable(tuple.get(reply.count()))
                                .map(Long::intValue).orElse(0)
                ));

        return tuples.stream()
                .map(t -> {
                    Long commentId = t.get(animeComment.id);

                    CommentStatus status = t.get(animeComment.status);
                    Integer replyCount = replyCountMap.getOrDefault(commentId, 0);
                    if (status != CommentStatus.NORMAL && replyCount == 0) {
                        return null;
                    }

                    Long authorId = t.get(animeComment.author.id);
                    boolean canDelete = Objects.equals(authorId, principalId) || isAdmin;

                    return CommentDto.builder()
                            .status(status)
                            .commentId(commentId)

                            .canDeleteThis(canDelete)

                            .isLiked(t.get(isLikedExpression))
                            .commentLikeId(t.get(likeIdSubquery))
                            .likeCount(
                                    Optional.ofNullable(t.get(likeCountAlias.count()))
                                            .map(Long::intValue).orElse(0)
                            )

                            .authorId(authorId)
                            .nickname(t.get(animeComment.author.nickname))
                            .profileImageUrl(t.get(animeComment.author.profileImageUrl))
                            .voteCount(t.get(animeComment.voteCount))
                            .episodeNumber(t.get(animeComment.episode.episodeNumber))

                            .createdAt(t.get(animeComment.createdAt))
                            .attachedImageUrl(t.get(animeComment.attachedImageUrl))
                            .body(t.get(animeComment.body))

                            .replyCount(replyCount)
                            .build();
                })
                .toList();
    }

    @Override
    public Integer countTotalElements(Long animeId, List<Long> episodeIds) {
        Long commentsCount = queryFactory.select(animeComment.count())
                .from(animeComment)
                .join(animeComment.episode, episode)
                .where(
                        animeComment.anime.id.eq(animeId),
                        episode.id.in(episodeIds),
                        // 방영 주간
                        animeComment.createdAt.goe(episode.scheduledAt)
                                .and(animeComment.createdAt.lt(episode.nextEpScheduledAt))
                        // 에피소드 댓글
                        .or(animeComment.episode.id.eq(episode.id))
                )
                .fetchOne();

        Long repliesCount = queryFactory.select(reply.count())
                .from(reply)
                .join(reply.parent, comment)
                .join(animeComment).on(animeComment.eq(comment))
                .join(animeComment.episode, episode)
                .where(
                        animeComment.anime.id.eq(animeId),
                        episode.id.in(episodeIds),
                        // 방영 주간
                        animeComment.createdAt.goe(episode.scheduledAt)
                                .and(animeComment.createdAt.lt(episode.nextEpScheduledAt))
                        // 에피소드 댓글
                        .or(animeComment.episode.id.eq(episode.id))
                )
                .fetchOne();

        return Optional.ofNullable(commentsCount).orElse(0L).intValue() +
                Optional.ofNullable(repliesCount).orElse(0L).intValue();
    }

    private OrderSpecifier<?>[] getOrder(QCommentLike likeCountAlias, CommentSortType sortBy) {
        return switch (sortBy) {
            case POPULAR -> new OrderSpecifier<?>[]{
                    likeCountAlias.count().desc(),
                    animeComment.createdAt.desc()
            };
            case RECENT -> new OrderSpecifier<?>[]{animeComment.createdAt.desc()};
            case OLDEST -> new OrderSpecifier<?>[]{animeComment.createdAt.asc()};
        };
    }
}
