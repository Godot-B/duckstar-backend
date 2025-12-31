package com.duckstar.domain.mapping.surveyVote;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Quarter;
import com.duckstar.domain.Survey;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.MedalType;
import com.duckstar.domain.enums.Medium;
import com.duckstar.service.ChartService;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.duckstar.service.ChartService.*;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quarter_id")
    private Quarter quarter;

    // anime 관계 필수 아님
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id")
    private Anime anime;

    @Column(nullable = false)
    private String title;

    @Column(length = 1024)
    private String imageUrl;

    @Column(length = 1024)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private Medium medium;

    //=== 순위/득표 정보 ===//

    @Column(name = "`rank`")
    private Integer rank;
    private Double votePercent;

    private Integer votes = 0;  // bonus 점수는 소수점 탈락
    private Integer voterCount = 0;

    private Double normalPercent;
    private Double bonusPercent;

    private Double malePercent;
    private Double femalePercent;

    private Double under14Percent;
    private Double to19Percent;
    private Double to24Percent;
    private Double to29Percent;
    private Double to34Percent;
    private Double over35Percent;

    protected SurveyCandidate(
            Survey survey,
            Quarter quarter,
            Anime anime,
            String title,
            String imageUrl,
            String thumbnailUrl,
            Medium medium
    ) {
        this.survey = survey;
        this.quarter = quarter;
        this.anime = anime;
        this.title = title;
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.medium = medium;
    }

    public static SurveyCandidate create(
            Survey survey,
            Quarter quarter,
            Anime anime,
            String title,
            String imageUrl,
            String thumbnailUrl,
            Medium medium
    ) {
        return new SurveyCandidate(
                survey,
                quarter,
                anime,
                title,
                imageUrl,
                thumbnailUrl,
                medium
        );
    }

    public static SurveyCandidate createByAnime(
            Survey survey,
            Quarter quarter,
            Anime anime
    ) {
        return new SurveyCandidate(
                survey,
                quarter,
                anime,
                anime.getTitleKor(),
                anime.getMainImageUrl(),
                anime.getMainThumbnailUrl(),
                anime.getMedium()
        );
    }

    public void updateImage(String imageUrl, String thumbnailUrl) {
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setAnime(Anime anime) {
        this.anime = anime;
    }

    public void updateStatistics(SurveyStatRecord record) {
        // 보너스(50점) 존재 시 내리기
        int score = nvl(record.score());
        if (score % 100 != 0) {
            score = score - 50;
        }
        this.votes = score / 100;
        this.voterCount = (int) nvl(record.voterCount());

        // 1. 표 종류 퍼센트
        int stampCount = nvl(record.normalCount()) + nvl(record.bonusCount());
        this.normalPercent = calculatePercentage(record.normalCount(), stampCount);
        this.bonusPercent = (stampCount > 0) ? 100.0 - this.normalPercent : 0.0;

        // 2. 성별 퍼센트
        int genderCount = nvl(record.maleCount()) + nvl(record.femaleCount());
        this.malePercent = calculatePercentage(record.maleCount(), genderCount);
        this.femalePercent = (genderCount > 0) ? 100.0 - this.malePercent : 0.0;

        // 3. 연령대 퍼센트
        int ageGroupCount = nvl(record.under14()) + nvl(record.age1519()) + nvl(record.age2024()) +
                nvl(record.age2529()) + nvl(record.age3034()) + nvl(record.over35());

        this.under14Percent = calculatePercentage(record.under14(), ageGroupCount);
        this.to19Percent = calculatePercentage(record.age1519(), ageGroupCount);
        this.to24Percent = calculatePercentage(record.age2024(), ageGroupCount);
        this.to29Percent = calculatePercentage(record.age2529(), ageGroupCount);
        this.to34Percent = calculatePercentage(record.age3034(), ageGroupCount);
        this.over35Percent = calculatePercentage(record.over35(), ageGroupCount);
    }

    private double calculatePercentage(Integer count, int total) {
        if (total == 0 || count == null) return 0.0;
        return (count / (double) total) * 100;
    }

    private int nvl(Integer val) {
        return val == null ? 0 : val;
    }

    private long nvl(Long val) {
        return val == null ? 0 : val;
    }

    public void setRankAndVotePercent(int rank, double votePercent) {
        this.rank = rank;
        this.votePercent = votePercent;
    }
}
