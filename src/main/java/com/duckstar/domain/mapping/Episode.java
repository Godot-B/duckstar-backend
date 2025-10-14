package com.duckstar.domain.mapping;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Week;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.vo.RankInfo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
                @Index(name = "idx_episode_s",
                        columnList = "scheduled_at"),
        }
)
public class Episode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id", nullable = false)
    private Anime anime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id")
    private Week week;  // 순위 결정 시점에 셋팅

    private Integer episodeNumber;

    //=== 결방, 변칙 편성 update API 마련 필요 ===//

    private Boolean isBreak;

    private Boolean isRescheduled;

    private LocalDateTime scheduledAt;

    private LocalDateTime nextEpScheduledAt;

    private Boolean isVoteEnabled = true;

    // [별점 방식]
    private Integer voterCount = 0;  // 투표 수 (또는 투표자 수)

    // 추후 StarDistribution 테이블 분리
    private Integer star_0_5 = 0;
    private Integer star_1_0 = 0;
    private Integer star_1_5 = 0;
    private Integer star_2_0 = 0;
    private Integer star_2_5 = 0;
    private Integer star_3_0 = 0;
    private Integer star_3_5 = 0;
    private Integer star_4_0 = 0;
    private Integer star_4_5 = 0;
    private Integer star_5_0 = 0;

    private Double bayesScore = 0.0;

    @Embedded
    private RankInfo rankInfo;

    protected Episode(
            Anime anime,
            Integer episodeNumber,
            Boolean isBreak,
            Boolean isRescheduled,
            LocalDateTime scheduledAt,
            LocalDateTime nextEpScheduledAt
    ) {
        this.anime = anime;
        this.episodeNumber = episodeNumber;
        this.isBreak = isBreak;
        this.isRescheduled = isRescheduled;
        this.scheduledAt = scheduledAt;
        this.nextEpScheduledAt = nextEpScheduledAt;
    }

    public static Episode create(
            Anime anime,
            Integer episodeNumber,
            LocalDateTime scheduledAt,
            LocalDateTime nextEpScheduledAt
    ) {
        return new Episode(
                anime,
                episodeNumber,
                null,
                null,
                scheduledAt,
                nextEpScheduledAt
        );
    }

    public void addVoterCount() {
        voterCount += 1;
    }

    public void removeVoterCount() {
        voterCount -= 1;
    }

    public void addStar(int starScore) {
        switch (starScore) {
            case 1 -> this.star_0_5++;
            case 2 -> this.star_1_0++;
            case 3 -> this.star_1_5++;
            case 4 -> this.star_2_0++;
            case 5 -> this.star_2_5++;
            case 6 -> this.star_3_0++;
            case 7 -> this.star_3_5++;
            case 8 -> this.star_4_0++;
            case 9 -> this.star_4_5++;
            case 10 -> this.star_5_0++;
        }
    }

    public void removeStar(int oldScore) {
        switch (oldScore) {
            case 1 -> this.star_0_5--;
            case 2 -> this.star_1_0--;
            case 3 -> this.star_1_5--;
            case 4 -> this.star_2_0--;
            case 5 -> this.star_2_5--;
            case 6 -> this.star_3_0--;
            case 7 -> this.star_3_5--;
            case 8 -> this.star_4_0--;
            case 9 -> this.star_4_5--;
            case 10 -> this.star_5_0--;
        }
    }

    public void updateStar(Integer oldScore, int newScore) {
        if (oldScore != null) {
            removeStar(oldScore);
        }
        addStar(newScore);
    }

    public double getWeightedSum() {
        return 0.5 * star_0_5 +
                1.0 * star_1_0 +
                1.5 * star_1_5 +
                2.0 * star_2_0 +
                2.5 * star_2_5 +
                3.0 * star_3_0 +
                3.5 * star_3_5 +
                4.0 * star_4_0 +
                4.5 * star_4_5 +
                5.0 * star_5_0;
    }

    public double getStarAverage() {
        return (voterCount == 0) ? 0.0 : getWeightedSum() / (double) voterCount;
    }

    public void setStats(Integer voterCount, int[] scores) {
        this.voterCount = voterCount;
        this.star_0_5 = scores[0];
        this.star_1_0 = scores[1];
        this.star_1_5 = scores[2];
        this.star_2_0 = scores[3];
        this.star_2_5 = scores[4];
        this.star_3_0 = scores[5];
        this.star_3_5 = scores[6];
        this.star_4_0 = scores[7];
        this.star_4_5 = scores[8];
        this.star_5_0 = scores[9];
    }

    public void calculateBayesScore(int m, double C) {
        int v = voterCount;
        if (voterCount == 0) {
            this.bayesScore = 0.0;
            return;
        }

        double R = getStarAverage();

        this.bayesScore =
                (double) v / (v + m) * R +
                (double) m / (v + m) * C;
    }

    public void setRankInfo(Week week, RankInfo lastRankInfo, RankInfo rankInfo) {
        this.week = week;
        this.rankInfo = rankInfo;
        this.anime.updateRankInfo(lastRankInfo, rankInfo);
    }

    public void setIsVoteEnabled(boolean isVoteEnabled) {
        this.isVoteEnabled = isVoteEnabled;
    }

    public int[] getStarList() {
        return new int[]{star_0_5, star_1_0, star_1_5, star_2_0, star_2_5, star_3_0, star_3_5, star_4_0, star_4_5, star_5_0};
    }
}
