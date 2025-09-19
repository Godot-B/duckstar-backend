package com.duckstar.domain.mapping;

import com.duckstar.domain.Anime;
import com.duckstar.domain.vo.RankInfo;
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

    @Column(nullable = false)
    private Integer votes = 0;  // bonus 점수는 소수점 탈락

    private Integer voterCount = 0;

    private Integer maleCount = 0;

    private Integer femaleCount = 0;

    @Embedded
    private RankInfo rankInfo;

    protected AnimeCandidate(Week week, Anime anime) {
        this.week = week;
        this.anime = anime;
    }

    public static AnimeCandidate create(Week week, Anime anime) {
        return new AnimeCandidate(week, anime);
    }

    public void updateInfo(int votes, int voterCount, int femaleCount) {
        this.votes = votes;
        this.voterCount = voterCount;
        this.femaleCount = femaleCount;

        this.maleCount = voterCount - femaleCount;
    }

    public void setRankInfo(RankInfo rankInfo) {
        this.rankInfo = rankInfo;
    }
}