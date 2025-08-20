package com.duckstar.domain.mapping.comment;

import com.duckstar.domain.Member;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.CommentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    private Integer voteCount;

    @Column(length = 512)
    private String attachedImageUrl;

    @Lob
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private CommentStatus status = CommentStatus.NORMAL;

    protected Comment(
            Member author,
            Integer voteCount,
            String attachedImageUrl,
            String body
    ) {
        this.author = author;
        this.voteCount = voteCount;
        this.attachedImageUrl = attachedImageUrl;
        this.body = body;
    }

    public void setStatus(CommentStatus status) {
        this.status = status;
    }

    public boolean isDeleted() {
        return status == CommentStatus.DELETED || status == CommentStatus.ADMIN_DELETED;
    }
}
