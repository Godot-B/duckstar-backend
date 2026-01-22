package com.duckstar.repository.Reply;

import com.duckstar.domain.Member;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.QReply;
import com.duckstar.domain.mapping.QReplyLike;
import com.duckstar.security.MemberPrincipal;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

import static com.duckstar.web.dto.CommentResponseDto.*;

@Repository
@RequiredArgsConstructor
public class ReplyRepositoryCustomImpl implements ReplyRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QReply reply = QReply.reply;
    private final QReplyLike replyLike = QReplyLike.replyLike;

    @Override
    public List<ReplyDto> getReplyDtos(
            Long commentId,
            MemberPrincipal principal,
            int offset,
            int limit
    ) {
        Long principalId;
        boolean isAdmin;

        Expression<Long> likeIdSubquery;
        Expression<Boolean> isLikedExpression;

        if (principal != null) {
            principalId = principal.getId();
            isAdmin = principal.isAdmin();

            likeIdSubquery = JPAExpressions
                    .select(replyLike.id)
                    .from(replyLike)
                    .where(
                            replyLike.reply.id.eq(reply.id),
                            replyLike.member.id.eq(principalId)
                    )
                    .limit(1);

            isLikedExpression = JPAExpressions
                    .select(replyLike.isLiked)
                    .from(replyLike)
                    .where(
                            replyLike.reply.id.eq(reply.id),
                            replyLike.member.id.eq(principalId)
                    )
                    .limit(1);
        } else {
            principalId = null;
            isAdmin = false;

            likeIdSubquery = Expressions.nullExpression(Long.class);
            isLikedExpression = Expressions.constant(false);
        }

        List<Tuple> tuples = queryFactory.select(
                        likeIdSubquery,
                        isLikedExpression,
                        reply.status,
                        reply.id,
                        reply.likeCount,
                        reply.author.id,
                        reply.author.nickname,
                        reply.author.profileImageUrl,
                        reply.voteCount,
                        reply.createdAt,
                        reply.listener,
                        reply.attachedImageUrl,
                        reply.body
                )
                .from(reply)
                .leftJoin(reply.listener) // 명시적으로 leftJoin
                .where(
                        reply.parent.id.eq(commentId),
                        reply.status.ne(CommentStatus.DELETED).and(
                                reply.status.ne(CommentStatus.ADMIN_DELETED)
                        )
                )
                .orderBy(reply.createdAt.asc())
                .offset(offset)
                .limit(limit)
                .fetch();

        List<Long> replyIds = tuples.stream()
                .map(t -> t.get(reply.id))
                .toList();
        if (replyIds.isEmpty()) {
            return List.of();
        }

        return tuples.stream()
                .map(t -> {
                    Long authorId = t.get(reply.author.id);
                    boolean canDelete = Objects.equals(authorId, principalId) || isAdmin;

                    System.out.println("replyId: " + t.get(reply.id));
                    Member listener = t.get(reply.listener);
                    System.out.println("========================");

                    return ReplyDto.builder()
                            .status(t.get(reply.status))
                            .replyId(t.get(reply.id))

                            .canDeleteThis(canDelete)

                            .isLiked(t.get(isLikedExpression))
                            .replyLikeId(t.get(likeIdSubquery))
                            .likeCount(t.get(reply.likeCount))

                            .authorId(authorId)
                            .nickname(t.get(reply.author.nickname))
                            .profileImageUrl(t.get(reply.author.profileImageUrl))
                            .voteCount(t.get(reply.voteCount))

                            .createdAt(t.get(reply.createdAt))
                            .listenerNickname(listener == null ? "" : listener.getNickname())
                            .attachedImageUrl(t.get(reply.attachedImageUrl))
                            .body(t.get(reply.body))
                            .build();
                })
                .toList();
    }
}
