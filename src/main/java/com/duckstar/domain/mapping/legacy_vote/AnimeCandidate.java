package com.duckstar.domain.mapping.legacy_vote;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Week;
import com.duckstar.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
                @Index(name = "idx_anime_candidate_a",
                        columnList = "anime_id"),
                @Index(name = "idx_anime_candidate_v",
                        columnList = "votes")
        }
)
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

    private Integer voterCount = 0;  // 투표 수 (또는 투표자 수)

    private Integer maleCount = 0;

    private Integer femaleCount = 0;

    // [단일 방식]
    private Integer votes = 0;  // [단일 투표 모드] bonus 점수는 소수점 탈락

    @Embedded
    private RankInfo rankInfo;

    protected AnimeCandidate(Week week, Anime anime) {
        this.week = week;
        this.anime = anime;
    }

    public static AnimeCandidate create(Week week, Anime anime) {
        return new AnimeCandidate(week, anime);
    }
//
//    public void updateInfo(int votes, int voterCount, int femaleCount) {
//        this.votes = votes;
//        this.voterCount = voterCount;
//        this.femaleCount = femaleCount;
//
//        this.maleCount = voterCount - femaleCount;
//    }
//
//    public void setRankInfo(RankInfo lastRankInfo, RankInfo rankInfo) {
//        this.rankInfo = rankInfo;
//        this.anime.updateRankInfo_legacy(lastRankInfo, rankInfo);
//    }
}