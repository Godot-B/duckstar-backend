package com.duckstar.domain;

import com.duckstar.domain.common.BaseEntity;
import com.duckstar.util.QuarterUtil;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
                @Index(name = "idx_quarter_yq",
                        columnList = "year_value, quarter_value")
        }
)
public class Quarter extends BaseEntity {

    // 차트용. 날짜의 엄밀함 보장 ⭕️

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer yearValue;

    @Column(nullable = false)
    private Integer quarterValue;

    private Boolean isPrepared = false;

    protected Quarter(Integer yearValue, Integer quarterValue) {
        this.yearValue = yearValue;
        this.quarterValue = quarterValue;
    }

    public static Quarter create(Integer yearValue, Integer quarterValue) {
        return new Quarter(yearValue, quarterValue);
    }
}
