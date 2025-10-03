package com.duckstar.domain;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.QuarterHandler;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.SeasonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
                @Index(name = "idx_season_yt",
                        columnList = "year_value, type_order")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_season_yt",
                        columnNames = {"year_value", "type_order"})
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
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private SeasonType type;

    @Column(nullable = false)
    private Integer typeOrder;

    private Boolean isPrepared = false;

    protected Season(
            Quarter quarter,
            Integer yearValue,
            SeasonType type,
            Integer typeOrder
    ) {
        this.quarter = quarter;
        this.yearValue = yearValue;
        this.type = type;
        this.typeOrder = typeOrder;
    }

    public static Season create(Integer yearValue, Quarter quarter) {

        SeasonType type;
        switch (quarter.getQuarterValue()) {
            case 1 -> type = SeasonType.WINTER;
            case 2 -> type = SeasonType.SPRING;
            case 3 -> type = SeasonType.SUMMER;
            case 4 -> type = SeasonType.AUTUMN;
            default -> throw new QuarterHandler(ErrorStatus.QUARTER_VALUE_REQUIRED);
        }

        return new Season(
                quarter,
                yearValue,
                type,
                type.getQuarter()
        );
    }
}
