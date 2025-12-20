package com.duckstar.domain.mapping.surveyVote;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Survey;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.MedalType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SurveyCandidate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(nullable = false)
    private String title;

    @Column(length = 1024)
    private String thumbnailUrl;

    // anime 관계 필수 아님
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id")
    private Anime anime;

    private Integer maleCount = 0;

    private Integer femaleCount = 0;

    private Integer votes = 0;  // bonus 점수는 소수점 탈락

    private Integer voterCount = 0;

    //=== 순위 정보 ===//
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10)")
    private MedalType type;

    @Column(name = "`rank`")
    private Integer rank;

    private Double votePercent;

    private Double malePercent;

    protected SurveyCandidate(Survey survey, Anime anime) {
        this.survey = survey;
        this.anime = anime;
    }

    public static SurveyCandidate create(Survey survey, Anime anime) {
        return new SurveyCandidate(survey, anime);
    }
}
