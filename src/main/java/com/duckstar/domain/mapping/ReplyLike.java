package com.duckstar.domain.mapping;

import com.duckstar.domain.Member;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.mapping.comment.Comment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
                @Index(name = "idx_reply_like_rm",
                        columnList = "reply_id, member_id"),
                @Index(name = "idx_reply_like_r",
                        columnList = "reply_id"),
        }
)
public class ReplyLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_id", nullable = false)
    private Reply reply;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private Boolean isLiked = true;

    protected ReplyLike(Reply reply, Member member) {
        this.reply = reply;
        this.member = member;
    }

    public static ReplyLike create(Reply reply, Member member) {
        reply.addLikeCount();
        return new ReplyLike(reply, member);
    }

    public void restoreLike() {
        isLiked = true;
        reply.addLikeCount();
    }

    public void discardLike() {
        isLiked = false;
        reply.removeLikeCount();
    }
}