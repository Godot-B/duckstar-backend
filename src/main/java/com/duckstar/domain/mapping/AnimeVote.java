package com.duckstar.domain.mapping;

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
                @Index(name = "idx_anime_candidate_wac",
                        columnList = "submission_id, anime_candidate_id"),
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_anime_candidate_ap",
                        columnNames = {"submission_id", "anime_candidate_id"})
        }
)
@SequenceGenerator(
        name = "anime_vote_seq",
        sequenceName = "anime_vote_sequence",  // 매핑할 데이터베이스 시퀀스 이름
        allocationSize = 100
)
public class AnimeVote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "anime_vote_seq")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private WeekVoteSubmission weekVoteSubmission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_candidate_id", nullable = false)
    private AnimeCandidate animeCandidate;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private BallotType ballotType;    // 보너스 2개 = 1표

    @Column(nullable = false)
    private Integer typeOrder;

    protected AnimeVote(
            WeekVoteSubmission weekVoteSubmission,
            AnimeCandidate animeCandidate,
            BallotType ballotType,
            Integer typeOrder
    ) {
        this.weekVoteSubmission = weekVoteSubmission;
        this.animeCandidate = animeCandidate;
        this.ballotType = ballotType;
        this.typeOrder = typeOrder;
    }

    public static AnimeVote create(
            WeekVoteSubmission weekVoteSubmission,
            AnimeCandidate animeCandidate,
            BallotType ballotType
    ) {
        int typeOrder = ballotType.getOrder();

        return new AnimeVote(
                weekVoteSubmission,
                animeCandidate,
                ballotType,
                typeOrder
        );
    }
}
