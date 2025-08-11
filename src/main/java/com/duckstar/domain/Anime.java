package com.duckstar.domain;

import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.AnimeStatus;
import com.duckstar.domain.enums.DayOfWeekShort;
import com.duckstar.domain.enums.Medium;
import com.duckstar.domain.enums.SiteType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Anime extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    private Medium medium;  // TVA, MOVIE

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private AnimeStatus status;     // 방영(상영) 상태

    private Integer totalEpisodes;

    private String titleKor;

    private String titleOrigin;

    private String titleEng;

    private LocalDate airDate;

    @Column(length = 4)
    @Enumerated(EnumType.STRING)
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

    @Column(length = 5)
    private String minAge;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<SiteType, String> officialSite = new HashMap<>();

    private Integer debutRank;

    private LocalDate debutDate;

    private Integer peakRank;

    private LocalDate PeakDate;

    private Integer weeksOnTop10;
}