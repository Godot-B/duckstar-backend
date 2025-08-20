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
@Table(
        indexes = {
                @Index(name = "idx_comment_cc",
                        columnList = "contentIdForIdx, created_at"),
                @Index(name = "idx_comment_c",
                        columnList = "created_at"),
        }
)
public abstract class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 1. 추상 클래스 유지
     * 2. 단일 테이블 전략 유지
     * 3. 복합 인덱스를 통한 정렬 성능 확보
     */
    @Column(nullable = false)
    private Long contentIdForIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
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
            Long contentIdForIdx,
            Member author,
            Integer voteCount,
            String attachedImageUrl,
            String body
    ) {
        this.contentIdForIdx = contentIdForIdx;
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
