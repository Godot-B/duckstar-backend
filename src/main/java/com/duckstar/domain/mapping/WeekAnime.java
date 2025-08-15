package com.duckstar.domain.mapping;

import com.duckstar.domain.Anime;
import com.duckstar.domain.vo.RankInfo;
import com.duckstar.domain.Week;
import com.duckstar.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeekAnime extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id", nullable = false)
    private Week week;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id", nullable = false)
    private Anime anime;

    private Boolean isBreak;    // TVA 결방 주 여부

    private LocalDateTime rescheduledAt;    // 변칙 편성 시간

    private LocalDateTime airDateTime;

    private Integer totalVoteCount;

    private Integer maleCount;

    private Integer femaleCount;

    @Embedded
    private RankInfo rankInfo;

    public Boolean isBreak() {
        return isBreak;
    }
}