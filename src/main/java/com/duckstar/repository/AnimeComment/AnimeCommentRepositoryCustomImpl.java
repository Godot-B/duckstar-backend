package com.duckstar.repository.AnimeComment;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.CommentHandler;
import com.duckstar.domain.QMember;
import com.duckstar.domain.enums.CommentSortType;
import com.duckstar.domain.mapping.Episode;
import com.duckstar.domain.mapping.QCommentLike;
import com.duckstar.domain.mapping.QEpisode;
import com.duckstar.domain.mapping.QReply;
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
        List<Tuple> commentTuples = queryFactory.select(
                        comment.status,
                        comment.id,
                        comment.author.id,
                        commentLike.isLiked,
                        commentLike.id,
                        comment.likeCount,
                        comment.author.nickname,
                        comment.author.profileImageUrl,
                        comment.voteCount,
                        comment.createdAt,
                        comment.attachedImageUrl,
                        comment.body
                )
                .from(comment)
                .join(episode).on(
                        comment.createdAt.between(episode.scheduledAt, episode.nextEpScheduledAt)
                )
                .leftJoin(commentLike).on(
                        commentLike.comment.id.eq(comment.id)
                        .and(commentLike.member.id.eq(principal.getId()))
                )
                .where(comment.anime.id.eq(animeId)
                        .and(episode.id.in(episodeIds)))
                .orderBy(getOrder(sortBy))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<Long> commentIds = commentTuples.stream()
                .map(t -> t.get(comment.id))
                .toList();
        if (commentIds.isEmpty()) {
            return List.of();
        }

        List<Tuple> replyTuples = queryFactory
                .select(reply.parent.id, reply.count())
                .from(reply)
                .where(reply.parent.id.in(commentIds))
                .groupBy(reply.parent.id)
                .fetch();

        Map<Long, Integer> replyCountMap = replyTuples.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(reply.parent.id),
                        tuple -> Optional.ofNullable(tuple.get(reply.count()))
                                .map(Long::intValue).orElse(0)
                ));

        return commentTuples.stream()
                .map(t -> {
                    Long commentId = t.get(comment.id);
                    Long authorId = t.get(comment.author.id);
                    boolean isAuthor = Objects.equals(authorId, principal.getId());
                    boolean isAdmin = principal.isAdmin();

                    return CommentDto.builder()
                            .status(t.get(comment.status))
                            .commentId(commentId)
                            .authorId(authorId)
                            .canDeleteThis(isAuthor || isAdmin)
                            .commentLikeId(t.get(commentLike.id))
                            .isLiked(t.get(commentLike.isLiked))
                            .likeCount(t.get(comment.likeCount))
                            .nickname(t.get(comment.author.nickname))
                            .profileImageUrl(t.get(comment.author.profileImageUrl))
                            .voteCount(t.get(comment.voteCount))
                            .createdAt(t.get(comment.createdAt))
                            .attachedImageUrl(t.get(comment.attachedImageUrl))
                            .body(t.get(comment.body))
                            .replyCount(
                                    replyCountMap.getOrDefault(commentId, 0)
                            )
                            .build();
                })
                .toList();
    }

    private OrderSpecifier<?>[] getOrder(CommentSortType sortBy) {
        return switch (sortBy) {
            case POPULAR -> new OrderSpecifier<?>[]{
                    comment.likeCount.desc(),
                    comment.createdAt.desc()
            };
            case RECENT -> new OrderSpecifier<?>[]{comment.createdAt.desc()};
            case OLDEST -> new OrderSpecifier<?>[]{comment.createdAt.asc()};
        };
    }
}
