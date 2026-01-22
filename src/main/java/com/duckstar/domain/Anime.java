package com.duckstar.domain;

import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.AnimeStatus;
import com.duckstar.domain.enums.DayOfWeekShort;
import com.duckstar.domain.enums.Medium;
import com.duckstar.domain.enums.SiteType;
import com.duckstar.domain.vo.RankInfo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private Medium medium;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    private AnimeStatus status;     // 방영(상영) 상태

    @Column(nullable = false)
    private String titleKor;

    private String titleOrigin;

    private String titleEng;

    /**
     * 에피소드는 벡터와 같다 (단, 1화는 예외가 많으므로 따로 두기: premiereDateTime)
     *  - 방향: dayOfWeek, airTime
     *  - 크기: totalEpisodes
     */
    private LocalDateTime premiereDateTime;  // 첫 방영 시간

    // 방향 설정: 정규 방송 기준
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10)")
    private DayOfWeekShort dayOfWeek;
    private LocalTime airTime;

    // 크기 설정
    private Integer totalEpisodes;  // TVA 경우 기본 12개

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

    @Lob
    private String synopsis;

    //=== 순위 정보 ===//

    // 스트릭 기록용
    private Integer lastRank;
    private Integer sameRankWeekStreak;

    private Integer debutRank;
    private LocalDate debutDate;

    private Integer peakRank;
    private LocalDate peakDate;

    private Integer weeksOnTop10;

    public void setStatus(AnimeStatus status) { this.status = status; }

    public void initRankInfo(Integer debutRank, LocalDate debutDate) {
        this.lastRank = debutRank;
        this.sameRankWeekStreak = 1;

        this.debutRank = debutRank;
        this.debutDate = debutDate;
        this.peakRank = debutRank;
        this.peakDate = debutDate;

        this.weeksOnTop10 = (debutRank != null && debutRank <= 10) ? 1 : 0;
    }

    public void updateRankInfo(RankInfo newRankInfo) {
        this.lastRank = newRankInfo.getRank();
        this.sameRankWeekStreak = newRankInfo.getConsecutiveWeeksAtSameRank();
        this.peakRank = newRankInfo.getPeakRank();
        this.peakDate = newRankInfo.getPeakDate();
        this.weeksOnTop10 = newRankInfo.getWeeksOnTop10();
    }

    public void updateImage(String mainImageUrl, String mainThumbnailUrl) {
        this.mainImageUrl = mainImageUrl;
        this.mainThumbnailUrl = mainThumbnailUrl;
    }

    public void setTotalEpisodes(Integer totalEpisodes) {
        this.totalEpisodes = totalEpisodes;
    }
}