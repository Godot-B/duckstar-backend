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

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private ContentType contentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id")
    private Anime anime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private Character character;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private BannerType bannerType;

    private Integer bannerNumber;

    private String animeImageUrl;

    private String characterImageUrl;

    protected HomeBanner(
            Week week,
            ContentType contentType,
            Anime anime,
            Character character,
            BannerType bannerType,
            Integer bannerNumber
    ) {
        this.week = week;
        this.contentType = contentType;
        this.anime = anime;
        this.character = character;
        this.bannerType = bannerType;
        this.bannerNumber = bannerNumber;
    }

    public static HomeBanner createByAnime(
            Week week,
            Integer bannerNumber,
            BannerType bannerType,
            Anime anime
    ) {
        return new HomeBanner(
                week,
                ContentType.ANIME,
                anime,
                null,
                bannerType,
                bannerNumber
        );
    }
}
