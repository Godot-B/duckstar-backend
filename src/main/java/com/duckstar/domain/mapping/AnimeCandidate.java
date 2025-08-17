package com.duckstar.domain.mapping;

import com.duckstar.domain.Anime;
import com.duckstar.domain.vo.RankInfo;
import com.duckstar.domain.Week;
import com.duckstar.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnimeCandidate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id", nullable = false)
    private Week week;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id", nullable = false)
    private Anime anime;

    private Integer totalVoteCount;

    private Integer maleCount;

    private Integer femaleCount;

    @Embedded
    private RankInfo rankInfo;
}