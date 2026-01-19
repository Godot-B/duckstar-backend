package com.duckstar.domain.mapping;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Quarter;
import com.duckstar.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnimeQuarter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id", nullable = false)
    private Anime anime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quarter_id", nullable = false)
    private Quarter quarter;

    protected AnimeQuarter(Anime anime, Quarter quarter) {
        this.anime = anime;
        this.quarter = quarter;
    }

    public static AnimeQuarter create(Anime anime, Quarter quarter) {
        return new AnimeQuarter(anime, quarter);
    }
}