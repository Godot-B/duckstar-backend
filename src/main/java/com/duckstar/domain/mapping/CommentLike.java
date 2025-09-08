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
                @Index(name = "idx_comment_like_cm",
                        columnList = "comment_id, member_id"),
                @Index(name = "idx_comment_like_c",
                        columnList = "comment_id")
        }
)
public class CommentLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private Boolean isLiked = true;

    protected CommentLike(Comment comment, Member member) {
        this.comment = comment;
        this.member = member;
    }

    public static CommentLike create(Comment comment, Member member) {
        comment.addLikeCount();
        return new CommentLike(comment, member);
    }

    public void restoreLike() {
        if (!isLiked) {
            isLiked = true;
            comment.addLikeCount();
        }
    }

    public void discardLike() {
        if (isLiked) {
            isLiked = false;
            comment.removeLikeCount();
        }
    }
}