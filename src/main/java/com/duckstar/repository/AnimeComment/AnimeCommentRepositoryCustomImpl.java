package com.duckstar.repository.AnimeComment;

import com.duckstar.domain.enums.CommentSortType;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.QCommentLike;
import com.duckstar.domain.mapping.QEpisode;
import com.duckstar.domain.mapping.QReply;
import com.duckstar.domain.mapping.comment.AnimeComment;
import com.duckstar.domain.mapping.comment.QAnimeComment;
import com.duckstar.security.MemberPrincipal;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.duckstar.web.dto.CommentResponseDto.*;

@Repository
@RequiredArgsConstructor
public class AnimeCommentRepositoryCustomImpl implements AnimeCommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QAnimeComment comment = QAnimeComment.animeComment;
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

        BooleanBuilder joinCondition = new BooleanBuilder()
                .and(commentLike.comment.id.eq(comment.id));

        if (principal != null) {
            principalId = principal.getId();
            isAdmin = principal.isAdmin();

            joinCondition.and(commentLike.member.id.eq(principalId));
        } else {
            principalId = null;
            isAdmin = false;
        }

        List<Tuple> tuples = queryFactory.select(
                        comment.status,
                        comment.id,
                        commentLike.isLiked,
                        commentLike.id,
                        likeCountAlias.count(),
                        comment.author.id,
                        comment.author.nickname,
                        comment.author.profileImageUrl,
                        comment.voteCount,
                        comment.episode.episodeNumber,
                        comment.createdAt,
                        comment.attachedImageUrl,
                        comment.body
                )
                .from(comment)
                .join(episode).on(
                        // 방영 주간
                        comment.createdAt.goe(episode.scheduledAt)
                                .and(comment.createdAt.lt(episode.nextEpScheduledAt))
                        // 에피소드 댓글
                                .or(comment.episode.id.eq(episode.id))
                )
                .leftJoin(likeCountAlias).on(likeCountAlias.comment.id.eq(comment.id))
                .leftJoin(commentLike).on(joinCondition)
                .where(comment.contentIdForIdx.eq(animeId),
                        (episode.id.in(episodeIds)))
                .groupBy(comment.id)  // 안전용 명시
                .orderBy(getOrder(likeCountAlias, sortBy))  // 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<Long> commentIds = tuples.stream()
                .map(t -> t.get(comment.id))
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
                    Long commentId = t.get(comment.id);

                    CommentStatus status = t.get(comment.status);
                    Integer replyCount = replyCountMap.getOrDefault(commentId, 0);
                    if (status != CommentStatus.NORMAL && replyCount == 0) {
                        return null;
                    }

                    Long authorId = t.get(comment.author.id);
                    boolean canDelete = Objects.equals(authorId, principalId) || isAdmin;

                    return CommentDto.builder()
                            .status(status)
                            .commentId(commentId)

                            .canDeleteThis(canDelete)

                            .isLiked(t.get(commentLike.isLiked))
                            .commentLikeId(t.get(commentLike.id))
                            .likeCount(
                                    Optional.ofNullable(t.get(likeCountAlias.count()))
                                            .map(Long::intValue).orElse(0)
                            )

                            .authorId(authorId)
                            .nickname(t.get(comment.author.nickname))
                            .profileImageUrl(t.get(comment.author.profileImageUrl))
                            .voteCount(t.get(comment.voteCount))
                            .episodeNumber(t.get(comment.episode.episodeNumber))

                            .createdAt(t.get(comment.createdAt))
                            .attachedImageUrl(t.get(comment.attachedImageUrl))
                            .body(t.get(comment.body))

                            .replyCount(replyCount)
                            .build();
                })
                .toList();
    }

    @Override
    public Integer countTotalElements(Long animeId, List<Long> episodeIds) {
        Long commentsCount = queryFactory.select(comment.count())
                .from(comment)
                .join(episode).on(
                        // 방영 주간
                        comment.createdAt.goe(episode.scheduledAt)
                                .and(comment.createdAt.lt(episode.nextEpScheduledAt))
                        // 에피소드 댓글
                                .or(comment.episode.id.eq(episode.id))
                )
                .where(comment.contentIdForIdx.eq(animeId),
                        episode.id.in(episodeIds))
                .fetchOne();

        Long repliesCount = queryFactory.select(reply.count())
                .from(reply)
                .join(reply.parent)
                .join(episode).on(
                        // 방영 주간
                        comment.createdAt.goe(episode.scheduledAt)
                                .and(comment.createdAt.lt(episode.nextEpScheduledAt))
                                // 에피소드 댓글
                                .or(comment.episode.id.eq(episode.id))
                )
                .where(comment.contentIdForIdx.eq(animeId),
                        episode.id.in(episodeIds))
                .fetchOne();

        return Optional.ofNullable(commentsCount).orElse(0L).intValue() +
                Optional.ofNullable(repliesCount).orElse(0L).intValue();
    }

    private OrderSpecifier<?>[] getOrder(QCommentLike likeCountAlias, CommentSortType sortBy) {
        return switch (sortBy) {
            case POPULAR -> new OrderSpecifier<?>[]{
                    likeCountAlias.count().desc(),
                    comment.createdAt.desc()
            };
            case RECENT -> new OrderSpecifier<?>[]{comment.createdAt.desc()};
            case OLDEST -> new OrderSpecifier<?>[]{comment.createdAt.asc()};
        };
    }
}
