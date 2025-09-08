package com.duckstar.domain.mapping;

import com.duckstar.domain.Member;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.comment.Comment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
                @Index(name = "idx_reply_p",
                        columnList = "parent_id"),
        }
)
public class Reply extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listener_id")
    private Member listener;

    private Integer voteCount;

    @Column(length = 512)
    private String attachedImageUrl;

    @Lob
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private CommentStatus status = CommentStatus.NORMAL;

    private Integer likeCount = 0;

    protected Reply(
            Comment parent,
            Member author,
            Member listener,
            Integer voteCount,
            String attachedImageUrl,
            String body
    ) {
        this.parent = parent;
        this.author = author;
        this.listener = listener;
        this.voteCount = voteCount;
        this.attachedImageUrl = attachedImageUrl;
        this.body = body;
    }

    public static Reply create(
            Comment parent,
            Member author,
            Member listener,
            Integer voteCount,
            String attachedImageUrl,
            String body
    ) {
        parent.addReply();
        return new Reply(
                parent,
                author,
                listener,
                voteCount,
                attachedImageUrl,
                body
        );
    }

    public void setStatus(CommentStatus status) {
        this.status = status;
    }

    public void addLikeCount() {
        likeCount += 1;
    }

    public void removeLikeCount() {
        if (likeCount > 0) likeCount -= 1;
    }
}
