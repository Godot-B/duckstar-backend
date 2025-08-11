package com.duckstar.domain.mapping;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Week;
import com.duckstar.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeekAnime extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id")
    private Week week;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id")
    private Anime anime;
    
    private Boolean isBreak;    // TVA 결방 주 여부

    @Column(name = "`rank`")
    private Integer rank;

    private Integer rankDiff;   // 분기 신작의 경우 null

    private Integer consecutiveWeeksAtSameRank;

    private Integer totalVoteCount;

    private Integer maleCount;

    private Integer femaleCount;

    // 기록용
    private Double votePercent;

    private Integer malePercent;

    private Integer peakRank;

    private LocalDate peakDate;

    private Integer weeksOnTop10;
}