package com.duckstar.domain.mapping.surveyVote;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Quarter;
import com.duckstar.domain.Survey;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.MedalType;
import com.duckstar.domain.enums.Medium;
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
}
