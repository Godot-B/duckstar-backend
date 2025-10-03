package com.duckstar.domain.mapping;

import com.duckstar.domain.Member;
import com.duckstar.domain.Week;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.Gender;
import com.duckstar.domain.enums.VoteCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
                @Index(name = "idx_submission_wp",
                        columnList = "week_id, principal_key"),
                @Index(name = "idx_submission_wm",
                        columnList = "week_id, member_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_submission_wpc",
                        columnNames = {"week_id", "principal_key", "category"})
        }
)
public class WeekVoteSubmission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id")
    private Week week;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(length = 64)
    private String ipHash;

    @Column(length = 64)
    private String cookieId;

    // 인덱스
    @Column(length = 80, nullable = false)
    private String principalKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private VoteCategory category;

    protected WeekVoteSubmission(
            Week week,
            Member member,
            String cookieId,
            String ipHash,
            String principalKey,
            Gender gender,
            VoteCategory category
    ) {
        this.week = week;
        this.member = member;
        this.cookieId = cookieId;
        this.ipHash = ipHash;
        this.principalKey = principalKey;
        this.gender = gender;
        this.category = category;
    }

    public static WeekVoteSubmission create(
            Week week,
            Member member,
            String cookieId,
            String ipHash,
            String principalKey,
            Gender gender,
            VoteCategory category
    ) {
        return new WeekVoteSubmission(
                week,
                member,
                cookieId,
                ipHash,
                principalKey,
                gender,
                category
        );
    }

    public void setMember(Member member, String principalKey) {
        this.member = member;
        this.principalKey = principalKey;
    }
}
