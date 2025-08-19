package com.duckstar.domain.mapping;

import com.duckstar.domain.Member;
import com.duckstar.domain.Week;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.VoteCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_week_principal_category",
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
    private String cookieId;

    @Column(length = 80, nullable = false)
    private String principalKey;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private VoteCategory category;

    protected WeekVoteSubmission(
            Week week,
            Member member,
            String cookieId,
            String principalKey,
            VoteCategory category
    ) {
        this.week = week;
        this.member = member;
        this.cookieId = cookieId;
        this.principalKey = principalKey;
        this.category = category;
    }

    public static WeekVoteSubmission create(
            Week week,
            Member member,
            String cookieId,
            String principalKey,
            VoteCategory category
    ) {
        return new WeekVoteSubmission(
                week,
                member,
                cookieId,
                principalKey,
                category
        );
    }
}
