package com.duckstar.domain;

import com.duckstar.domain.enums.MedalType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankInfo {

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private MedalType type;

    @Column(name = "`rank`", nullable = false)
    private Integer rank;

    private Integer rankDiff;

    private Integer consecutiveWeeksAtSameRank;

    @Column(nullable = false)
    private Double votePercent;

    @Column(nullable = false)
    private Double malePercent;

    @Column(nullable = false)
    private Integer peakRank;

    @Column(nullable = false)
    private LocalDate peakDate;

    @Column(nullable = false)
    private Integer weeksOnTop10;
}