package com.duckstar.domain.vo;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.RankHandler;
import com.duckstar.domain.enums.MedalType;
import jakarta.persistence.*;
import lombok.AccessLevel;
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

    private Integer consecutiveWeeksAtSameRank = 0;

    private Integer peakRank;

    private LocalDate peakDate;

    private Integer weeksOnTop10;

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

        boolean isPrized = false;
        boolean isTop10 = false;
        if (rank <= 3) {
            isPrized = true;
            isTop10 = true;
        } else if (rank <= 10) {
            isTop10 = true;
        }

        int medalIdx = isPrized ? rank - 1 : 3;
        RankInfo rankInfo = new RankInfo(
                MedalType.values()[medalIdx],
                rank,
                votePercent,
                malePercent,
                isTop10 ? 1 : 0
        );

        boolean notNew = lastRankInfo != null;

        if (notNew) {
            Integer lastRank = lastRankInfo.getRank();
            rankInfo.rankDiff = lastRank - rank;

            if (rankInfo.rankDiff == 0)
                rankInfo.consecutiveWeeksAtSameRank = lastRankInfo.getConsecutiveWeeksAtSameRank() + 1;

            if (isTop10)
                rankInfo.weeksOnTop10 = lastRankInfo.getWeeksOnTop10() + 1;

            Integer peakRank = lastRankInfo.getPeakRank();
            if (rank < peakRank) {
                rankInfo.peakRank = rank;
                rankInfo.peakDate = today;
            } else {
                rankInfo.peakRank = peakRank;
                rankInfo.peakDate = lastRankInfo.getPeakDate();
            }
        } else {
            rankInfo.peakRank = rank;
            rankInfo.peakDate = today;
        }

        return rankInfo;
    }
}