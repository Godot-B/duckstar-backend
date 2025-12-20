package com.duckstar.domain.mapping.surveyVote;

import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.BallotType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
                @Index(name = "idx_survey_vote_s",
                        columnList = "submission_id"),
                @Index(name = "idx_survey_vote_c",
                        columnList = "candidate_id"),
        }
)
public class SurveyVote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private SurveyVoteSubmission surveyVoteSubmission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private SurveyCandidate surveyCandidate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private BallotType ballotType;    // 보너스 2개 = 1표

    @Column(nullable = false)
    private Integer score;

    protected SurveyVote(
            SurveyVoteSubmission surveyVoteSubmission,
            SurveyCandidate surveyCandidate,
            BallotType ballotType,
            Integer score
    ) {
        this.surveyVoteSubmission = surveyVoteSubmission;
        this.surveyCandidate = surveyCandidate;
        this.ballotType = ballotType;
        this.score = score;
    }

    public static SurveyVote create(
            SurveyVoteSubmission surveyVoteSubmission,
            SurveyCandidate surveyCandidate,
            BallotType ballotType
    ) {
        int typeOrder = ballotType.getScore();

        return new SurveyVote(
                surveyVoteSubmission,
                surveyCandidate,
                ballotType,
                typeOrder
        );
    }

    public void updateScore(BallotType type) {
        this.ballotType = type;
        this.score = type.getScore();
    }
}
