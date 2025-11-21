package com.duckstar.domain.mapping;

import com.duckstar.domain.Member;
import com.duckstar.domain.Week;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.ContentType;
import com.duckstar.domain.enums.Gender;
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
    private String cookieId;

    @Column(length = 64)
    private String ipHash;

    private String userAgent;

    @Column(length = 64)
    private String fpHash;

    private Boolean isBlocked = false;

    // 인덱스
    @Column(length = 80, nullable = false)
    private String principalKey;

    @Enumerated(EnumType.STRING)
    private Gender gender;  // 일반 투표 방식에서 필요

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private ContentType category;

    protected WeekVoteSubmission(
            Boolean isBlocked,
            Week week,
            Member member,
            String cookieId,
            String ipHash,
            String userAgent,
            String fpHash,
            String principalKey,
            Gender gender,
            ContentType category
    ) {
        this.isBlocked = isBlocked;
        this.week = week;
        this.member = member;
        this.cookieId = cookieId;
        this.ipHash = ipHash;
        this.userAgent = userAgent;
        this.fpHash = fpHash;
        this.principalKey = principalKey;
        this.gender = gender;
        this.category = category;
    }

    public static WeekVoteSubmission create(
            boolean isBlocked,
            Week week,
            Member member,
            String cookieId,
            String ipHash,
            String userAgent,
            String fpHash,
            String principalKey,
            Gender gender,
            ContentType category
    ) {
        return new WeekVoteSubmission(
                isBlocked,
                week,
                member,
                cookieId,
                ipHash,
                userAgent,
                fpHash,
                principalKey,
                gender,
                category
        );
    }

    public void setMember(Member member, String principalKey) {
        this.member = member;
        this.principalKey = principalKey;
    }

    public boolean isBlocked() { return isBlocked; }

    public void setBlocked(Boolean isBlocked) {
        this.isBlocked = isBlocked;
    }
}
