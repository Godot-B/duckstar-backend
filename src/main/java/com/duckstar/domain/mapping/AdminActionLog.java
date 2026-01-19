package com.duckstar.domain.mapping;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Member;
import com.duckstar.domain.Week;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.AdminTaskType;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminActionLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id")
    private Week week;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id")
    private Anime anime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id")
    private Episode episode;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(15)")
    private AdminTaskType adminTaskType;

    @Column(length = 64)
    private String targetIpHash;

    @Lob
    private String reason;

    private Boolean isUndoable;

    public void setIsUndoable(Boolean isUndoable) {
        this.isUndoable = isUndoable;
    }
}