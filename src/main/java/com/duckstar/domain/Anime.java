package com.duckstar.domain;

import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.AnimeStatus;
import com.duckstar.domain.enums.DayOfWeekShort;
import com.duckstar.domain.enums.Medium;
import com.duckstar.domain.enums.SiteType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
                @Index(name = "idx_anime_t",
                        columnList = "titleKor")
        }
)
@Builder
@AllArgsConstructor
public class Anime extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Medium medium;  // TVA, MOVIE

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AnimeStatus status;     // 방영(상영) 상태

    private Integer totalEpisodes;

    private LocalDateTime premiereDateTime;

    @Column(nullable = false)
    private String titleKor;

    private String titleOrigin;

    private String titleEng;

    @Enumerated(EnumType.STRING)
    @Column(length = 4)
    private DayOfWeekShort dayOfWeek;

    @Column(length = 5)
    private String airTime;

    @Column(length = 100)
    private String corp;

    @Column(length = 100)
    private String director;

    private String genre;

    @Column(length = 100)
    private String author;  // 원작

    private Integer minAge;  // 시청 등급

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<SiteType, String> officialSite = new HashMap<>();

    @Column(length = 1024)
    private String mainImageUrl;

    @Column(length = 1024)
    private String mainThumbnailUrl;

    private Integer debutRank;

    private LocalDate debutDate;

    private Integer peakRank;

    private LocalDate peakDate;

    private Integer weeksOnTop10;

    @Lob
    private String synopsis;
}