package com.duckstar.repository.Reply;

import com.duckstar.domain.mapping.QEpisode;
import com.duckstar.domain.mapping.QReply;
import com.duckstar.domain.mapping.QReplyLike;
import com.duckstar.security.MemberPrincipal;
import com.querydsl.core.Tuple;
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
public class ReplyRepositoryCustomImpl implements ReplyRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QReply reply = QReply.reply;
    private final QReplyLike replyLike = QReplyLike.replyLike;

    @Override
    public List<ReplyDto> getReplyDtos(
            Long commentId,
            Pageable pageable,
            MemberPrincipal principal
    ) {
        QReplyLike replyLikeCountAlias = new QReplyLike("replyLikeCountAlias");

        List<Tuple> tuples = queryFactory.select(
                        reply.status,
                        reply.id,
                        replyLike.isLiked,
                        replyLike.id,
                        replyLikeCountAlias.count(),
                        reply.author.id,
                        reply.author.nickname,
                        reply.author.profileImageUrl,
                        reply.voteCount,
                        reply.createdAt,
                        reply.listener.id,
                        reply.attachedImageUrl,
                        reply.body
                )
                .from(reply)
                .leftJoin(replyLikeCountAlias).on(replyLikeCountAlias.reply.id.eq(reply.id))
                .leftJoin(replyLike).on(
                        replyLike.reply.id.eq(reply.id)
                                .and(replyLike.reply.id.eq(principal.getId())))
                .where(reply.parent.id.eq(commentId))
                .groupBy(reply.id)  // 안전용 명시
                .orderBy(reply.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<Long> replyIds = tuples.stream()
                .map(t -> t.get(reply.id))
                .toList();
        if (replyIds.isEmpty()) {
            return List.of();
        }

        return tuples.stream()
                .map(t -> {
                    Long replyId = t.get(reply.id);
                    Long authorId = t.get(reply.author.id);
                    boolean isAuthor = Objects.equals(authorId, principal.getId());
                    boolean isAdmin = principal.isAdmin();

                    return ReplyDto.builder()
                            .status(t.get(reply.status))
                            .replyId(replyId)

                            .canDeleteThis(isAuthor || isAdmin)

                            .isLiked(t.get(replyLike.isLiked))
                            .replyLikeId(t.get(replyLike.id))
                            .likeCount(
                                    Optional.ofNullable(t.get(replyLikeCountAlias.count()))
                                            .map(Long::intValue).orElse(0)
                            )

                            .authorId(authorId)
                            .nickname(t.get(reply.author.nickname))
                            .profileImageUrl(t.get(reply.author.profileImageUrl))
                            .voteCount(t.get(reply.voteCount))

                            .createdAt(t.get(reply.createdAt))
                            .listenerId(t.get(reply.listener.id))
                            .attachedImageUrl(t.get(reply.attachedImageUrl))
                            .body(t.get(reply.body))
                            .build();
                })
                .toList();
    }
}
