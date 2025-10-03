package com.duckstar.domain;

import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.BannerType;
import com.duckstar.domain.enums.ContentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HomeBanner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id", nullable = false)
    private Week week;

    private Integer bannerNumber;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private BannerType bannerType;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private ContentType contentType;

    @Column(nullable = false)
    private Long contentId;

    @Column(nullable = false)
    private String mainTitle;

    private String subTitle;

    @Column(nullable = false)
    private String animeImageUrl;

    private String characterImageUrl;

    protected HomeBanner(
            Week week,
            Integer bannerNumber,
            BannerType bannerType,
            ContentType contentType,
            Long contentId,
            String mainTitle,
            String subTitle,
            String animeImageUrl,
            String characterImageUrl
    ) {
        this.week = week;
        this.bannerNumber = bannerNumber;
        this.bannerType = bannerType;
        this.contentType = contentType;
        this.contentId = contentId;
        this.mainTitle = mainTitle;
        this.subTitle = subTitle;
        this.animeImageUrl = animeImageUrl;
        this.characterImageUrl = characterImageUrl;
    }

    public static HomeBanner createByAnime(
            Week week,
            Integer bannerNumber,
            BannerType bannerType,
            Anime anime,
            String subTitle
    ) {
        return new HomeBanner(
                week,
                bannerNumber,
                bannerType,
                ContentType.ANIME,
                anime.getId(),
                anime.getTitleKor(),
                subTitle,
                anime.getMainImageUrl(),
                null
        );
    }
}
