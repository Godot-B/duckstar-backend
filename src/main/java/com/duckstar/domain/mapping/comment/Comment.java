package com.duckstar.domain.mapping.comment;

import com.duckstar.domain.Member;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.weeklyVote.Episode;
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
                @Index(name = "idx_comment_cec",
                        columnList = "contentIdForIdx, episode_id, created_at"),
                @Index(name = "idx_comment_cc",
                        columnList = "contentIdForIdx, created_at"),
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
    @JoinColumn(name = "episode_id")
    private Episode episode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;

    @Column(insertable = false, updatable = false)
    private String dtype;  // 읽기 전용 필드

    private Boolean isUserTaggedEp;

    private Integer voteCount;

    @Column(length = 512)
    private String attachedImageUrl;

    @Lob
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(15)", nullable = false)
    protected CommentStatus status = CommentStatus.NORMAL;

    private Integer likeCount = 0;

    private Integer replyCount = 0;

    protected Comment(
            Long contentIdForIdx,
            Episode episode,
            Member author,
            Boolean isUserTaggedEp,
            Integer voteCount,
            String attachedImageUrl,
            String body
    ) {
        this.contentIdForIdx = contentIdForIdx;
        this.episode = episode;
        this.author = author;
        this.isUserTaggedEp = isUserTaggedEp;
        this.voteCount = voteCount;
        this.attachedImageUrl = attachedImageUrl;
        this.body = body;
    }

    public void addLikeCount() {
        likeCount += 1;
    }

    public void removeLikeCount() {
        if (likeCount > 0) likeCount -= 1;
    }

    public void addReply() {
        replyCount += 1;
    }

    public void removeReply() {
        if (replyCount > 0) replyCount -= 1;
    }

    public void setEpisode(Episode episode) {
        this.episode = episode;
    }

    public void updateBody(String body) {
        this.body = body;
    }
}
