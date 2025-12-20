package com.duckstar.domain.mapping.surveyVote;

import com.duckstar.domain.Member;
import com.duckstar.domain.Survey;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.AgeGroup;
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
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_submission_sp",
                        columnNames = {"survey_id", "principal_key"})
        },
        indexes = {
                @Index(name = "idx_submission_sm",
                        columnList = "survey_id, member_id")
        }
)
public class SurveyVoteSubmission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(length = 80, nullable = false)
    private String principalKey;

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

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(15)")
    private Gender gender = Gender.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    private AgeGroup ageGroup;

    protected SurveyVoteSubmission(
            Survey survey,
            String principalKey,
            Member member,
            String cookieId,
            String ipHash,
            String userAgent,
            String fpHash,
            Gender gender,
            AgeGroup ageGroup
    ) {
        this.survey = survey;
        this.principalKey = principalKey;
        this.member = member;
        this.cookieId = cookieId;
        this.ipHash = ipHash;
        this.userAgent = userAgent;
        this.fpHash = fpHash;
        this.gender = gender;
        this.ageGroup = ageGroup;
    }

    public static SurveyVoteSubmission create(
            Survey survey,
            String principalKey,
            Member member,
            String cookieId,
            String ipHash,
            String userAgent,
            String fpHash,
            Gender gender,
            AgeGroup ageGroup
    ) {
        return new SurveyVoteSubmission(
                survey,
                principalKey,
                member,
                cookieId,
                ipHash,
                userAgent,
                fpHash,
                gender,
                ageGroup
        );
    }

    public void setMember(Member member, String principalKey) {
        this.member = member;
        this.principalKey = principalKey;
    }
}
