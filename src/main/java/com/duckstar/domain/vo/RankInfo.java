package com.duckstar.domain.vo;

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
    @Column(length = 10)
    private MedalType type;

    @Column(name = "`rank`")
    private Integer rank;

    private Integer rankDiff;

    private Integer consecutiveWeeksAtSameRank;

    private Double votePercent;

    private Double malePercent;

    private Integer peakRank;

    private LocalDate peakDate;

    private Integer weeksOnTop10;
}