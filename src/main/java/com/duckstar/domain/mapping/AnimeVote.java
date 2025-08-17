package com.duckstar.domain.mapping;

import com.duckstar.domain.Member;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.VoteType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
                @Index(name = "idx_anime_candidate_ac",
                        columnList = "anime_candidate_id, created_at"),
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_anime_candidate_ap",
                        columnNames = {"anime_candidate_id", "principal_key"})
        }
)
@SequenceGenerator(
        name = "anime_vote_seq",
        sequenceName = "anime_vote_sequence",  // 매핑할 데이터베이스 시퀀스 이름
        initialValue = 1, allocationSize = 100
)
public class AnimeVote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "anime_vote_seq")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_candidate_id", nullable = false)
    private AnimeCandidate animeCandidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(length = 64)
    private String cookieId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private VoteType voteType;    // 보너스 2개 = 1표

    @Column( length = 80, nullable = false)
    private String principalKey;
}
