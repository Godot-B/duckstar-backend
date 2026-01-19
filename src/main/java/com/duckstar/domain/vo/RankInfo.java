package com.duckstar.domain.vo;

import com.duckstar.domain.Anime;
import com.duckstar.domain.enums.MedalType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankInfo {

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10)")
    private MedalType type;

    @Column(name = "`rank`")
    private Integer rank;

    // 아카이브용 (오픈 시간 외 에피소드 평가 개발 대비)
    private Double rankedAverage;
    private Integer rankedVoterCount;

    //=== 이전 기록 필요 ===//
    private Integer rankDiff;  // anime lastRank 없을 때 null 이며 NEW
    private Integer consecutiveWeeksAtSameRank;

    private Integer peakRank;
    private LocalDate peakDate;

    private Integer weeksOnTop10;

    @Builder
    protected RankInfo(
            MedalType type,
            Integer rank,
            Double rankedAverage,
            Integer rankedVoterCount
    ) {
        this.type = type;
        this.rank = rank;
        this.rankedAverage = rankedAverage;
        this.rankedVoterCount = rankedVoterCount;
    }

    public static RankInfo create(
            Anime anime,
            int rank,
            LocalDate lastWeekEndAt,
            Double rankedAverage,
            Integer rankedVoterCount
    ) {
        boolean isPrized = rank <= 3;
        boolean isTop10 = rank <= 10;

        int medalIdx = isPrized ? rank - 1 : 3;
        RankInfo newRankInfo = RankInfo.builder()
                .type(
                        MedalType.values()[medalIdx]
                )
                .rank(rank)
                .rankedAverage(rankedAverage)
                .rankedVoterCount(rankedVoterCount)
                .build();

        Integer lastRank = anime.getLastRank();
        boolean notNew = lastRank != null;

        if (notNew) {
            newRankInfo.rankDiff = lastRank - rank;

            if (newRankInfo.rankDiff.equals(0)) {
                newRankInfo.consecutiveWeeksAtSameRank = anime.getSameRankWeekStreak() + 1;
            } else {
                newRankInfo.consecutiveWeeksAtSameRank = 1;
            }

            Integer oldPeakRank = anime.getPeakRank();
            if (rank < oldPeakRank) {
                newRankInfo.peakRank = rank;
                newRankInfo.peakDate = lastWeekEndAt;
            } else {
                newRankInfo.peakRank = oldPeakRank;
                newRankInfo.peakDate = anime.getPeakDate();
            }

            newRankInfo.weeksOnTop10 = isTop10 ? anime.getWeeksOnTop10() + 1 : anime.getWeeksOnTop10();

            // anime 스트릭 갱신
            anime.updateRankInfo(newRankInfo);

        } else {
            newRankInfo.rankDiff = null;
            newRankInfo.consecutiveWeeksAtSameRank = 1;
            newRankInfo.peakRank = rank;
            newRankInfo.peakDate = lastWeekEndAt;
            newRankInfo.weeksOnTop10 = isTop10 ? 1 : 0;

            // anime 초기 랭크 정보 셋팅
            anime.initRankInfo(rank, lastWeekEndAt);
        }

        return newRankInfo;
    }
}
