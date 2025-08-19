package com.duckstar.domain.mapping;

import com.duckstar.domain.Member;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.comment.Comment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reply extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_member_id")
    private Member replyTo;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private CommentStatus status = CommentStatus.NORMAL;

    private Integer voteCount;

    @Column(length = 512)
    private String attachedImageUrl;

    @Lob
    private String body;

    private Integer likeCount;
}
