package com.duckstar.domain;

import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.SeasonType;
import com.duckstar.domain.mapping.AnimeSeason;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
                @Index(name = "idx_season_yt",
                        columnList = "year_value, type")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_season_yt",
                        columnNames = {"year_value", "type"})
        }
)
public class Season extends BaseEntity {

    // Anime 의 사회적 분류 카테고리. 날짜의 엄밀함 보장 ❌
    // 예: 3/31 방영 애니는 1분기지만 SPRING
    // 절기 판단 기준: 춘하추동, 방영일

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quarter_id")
    private Quarter quarter;

    @Column(nullable = false)
    private Integer yearValue;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private SeasonType type;

    @Column(nullable = false)
    private Integer typeOrder;
}
