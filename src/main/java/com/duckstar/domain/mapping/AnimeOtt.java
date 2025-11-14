package com.duckstar.domain.mapping;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Ott;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.web.dto.OttDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_anime_ott_ao",
                        columnNames = {"anime_id", "ott_id"})
        }
)
public class AnimeOtt extends BaseEntity {

    // 페치 조인 고려 필요

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id", nullable = false)
    private Anime anime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ott_id", nullable = false)
    private Ott ott;

    @Column(length = 512)
    private String watchUrl;

    protected AnimeOtt(Anime anime, Ott ott, String watchUrl) {
        this.anime = anime;
        this.ott = ott;
        this.watchUrl = watchUrl;
    }

    public static AnimeOtt create(Anime anime, Ott ott, String watchUrl) {
        return new AnimeOtt(anime, ott, watchUrl);
    }
}