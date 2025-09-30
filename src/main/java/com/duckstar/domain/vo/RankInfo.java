package com.duckstar.domain.vo;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.RankHandler;
import com.duckstar.domain.enums.MedalType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankInfo {

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10)")
    private MedalType type;

    @Column(name = "`rank`")
    private Integer rank;

    private Double votePercent;

    private Double malePercent;

    //=== 기존 기록 필요 ===//

    private Integer rankDiff;  // 이전 기록 없을 때 null 이며 NEW

    private Integer consecutiveWeeksAtSameRank;

    private Integer peakRank;

    private LocalDate peakDate;

    private Integer weeksOnTop10;

    @Builder
    protected RankInfo(
            MedalType type,
            Integer rank,
            Double votePercent,
            Double malePercent,
            Integer weeksOnTop10
    ) {
        this.type = type;
        this.rank = rank;
        this.votePercent = votePercent;
        this.malePercent = malePercent;
        this.weeksOnTop10 = weeksOnTop10;
    }

    public static RankInfo create(
            RankInfo lastRankInfo,
            LocalDate today,
            Integer rank,
            Double votePercent,
            Double malePercent
    ) {
        if (rank == null || rank <= 0) {
            throw new RankHandler(ErrorStatus.RANK_VALUE_NOT_VALID);
        }

        boolean isPrized = rank <= 3;
        boolean isTop10 = rank <= 10;

        int medalIdx = isPrized ? rank - 1 : 3;
        RankInfo newRankInfo = RankInfo.builder()
                .type(
                        MedalType.values()[medalIdx]
                )
                .rank(rank)
                .votePercent(votePercent)
                .malePercent(malePercent)
                .weeksOnTop10(isTop10 ? 1 : 0)
                .build();

        boolean notNew = lastRankInfo != null && lastRankInfo.getRank() >= 1;

        if (notNew) {
            Integer lastRank = lastRankInfo.getRank();
            newRankInfo.rankDiff = lastRank - rank;

            if (newRankInfo.rankDiff == 0) {
                newRankInfo.consecutiveWeeksAtSameRank = lastRankInfo.getConsecutiveWeeksAtSameRank() + 1;
            } else {
                newRankInfo.consecutiveWeeksAtSameRank = 1;
            }

            if (isTop10) {
                newRankInfo.weeksOnTop10 = lastRankInfo.getWeeksOnTop10() + 1;
            }

            Integer peakRank = lastRankInfo.getPeakRank();
            if (rank < peakRank) {
                newRankInfo.peakRank = rank;
                newRankInfo.peakDate = today;
            } else {
                newRankInfo.peakRank = peakRank;
                newRankInfo.peakDate = lastRankInfo.getPeakDate();
            }
        } else {
            newRankInfo.peakRank = rank;
            newRankInfo.peakDate = today;
            newRankInfo.consecutiveWeeksAtSameRank = 1;
        }

        return newRankInfo;
    }
}