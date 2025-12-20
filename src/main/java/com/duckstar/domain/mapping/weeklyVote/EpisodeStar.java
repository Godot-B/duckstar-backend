package com.duckstar.domain.mapping.weeklyVote;

import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.EpEvaluateState;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
                @Index(name = "idx_episode_star_s",
                        columnList = "submission_id"),
                @Index(name = "idx_episode_star_e",
                        columnList = "episode_id"),
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

    private Integer starScore;  // null: 별점 회수 , 1점 ~ 10점 :별 0.5개 ~ 5.0개

    private Boolean isLateParticipating = false;  // 늦참 투표로 생성됐는지 여부

    protected EpisodeStar(
            WeekVoteSubmission weekVoteSubmission,
            Episode episode,
            Integer starScore,
            Boolean isLateParticipating
    ) {
        this.weekVoteSubmission = weekVoteSubmission;
        this.episode = episode;
        this.starScore = starScore;
        this.isLateParticipating = isLateParticipating;
    }

    public static EpisodeStar create(
            boolean isBlocked,
            WeekVoteSubmission weekVoteSubmission,
            Episode episode,
            Integer starScore
    ) {
        if (!isBlocked) {
            // AnimeVote 와 다르게 바로바로 반영
            episode.addVoterCount();
            episode.addStar(starScore);
        }

        return new EpisodeStar(
                weekVoteSubmission,
                episode,
                starScore,
                episode.getEvaluateState() == EpEvaluateState.LOGIN_REQUIRED
        );
    }

    public void updateStarScore(boolean isBlocked, int newScore) {
        Integer oldScore = this.starScore;

        if (!isBlocked) {
            if (oldScore == null) {
                this.episode.addVoterCount();
            }
            episode.updateStar(oldScore, newScore);
        }

        this.starScore = newScore;
    }

    public void setWeekVoteSubmission(WeekVoteSubmission weekVoteSubmission) {
        this.weekVoteSubmission = weekVoteSubmission;
    }

    public void setStarScore(Integer starScore) {
        this.starScore = starScore;
    }

    public void withdrawScore() {
        int oldScore = this.starScore;
        this.starScore = null;
        this.episode.removeVoterCount();
        this.episode.removeStar(oldScore);
    }

    public boolean isLateParticipating() {
        return isLateParticipating;
    }
}
