package com.duckstar.domain.mapping;

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
                @Index(name = "idx_episode_star_e",
                        columnList = "episode_id"),
                @Index(name = "idx_episode_star_s",
                        columnList = "submission_id"),
        }
)
public class EpisodeStar extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private WeekVoteSubmission weekVoteSubmission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    @Column(nullable = false)
    private Integer starScore;  // 1점 ~ 10점 (별 0.5개 ~ 5.0개)

    protected EpisodeStar(
            WeekVoteSubmission weekVoteSubmission,
            Episode episode,
            Integer starScore
    ) {
        this.weekVoteSubmission = weekVoteSubmission;
        this.episode = episode;
        this.starScore = starScore;
    }

    public static EpisodeStar create(
            WeekVoteSubmission weekVoteSubmission,
            Episode episode,
            Integer starScore
    ) {
        // AnimeVote 와 다르게 바로바로 반영
        episode.addVoterCount();
        episode.addStar(starScore);

        return new EpisodeStar(
                weekVoteSubmission,
                episode,
                starScore
        );
    }

    public void updateStarScore(int newScore) {
        episode.updateStar(this.starScore, newScore);
        this.starScore = newScore;
    }
}
