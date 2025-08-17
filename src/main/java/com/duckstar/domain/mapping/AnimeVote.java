package com.duckstar.domain.mapping;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Member;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.VoteType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnimeVote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id", nullable = false)
    private Anime anime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(length = 64)
    private String cookieId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private VoteType voteType;    // 보너스 2개 = 1표
}
