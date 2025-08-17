package com.duckstar.domain;

import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.VoteStatus;
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
                @Index(name = "idx_week_q",
                        columnList = "quarter_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_week_qw",
                        columnNames = {"quarter_id", "week_value"})
        }
)
public class Week extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quarter_id", nullable = false)
    private Quarter quarter;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private VoteStatus status;

    @Column(nullable = false)
    private Integer weekValue;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column(nullable = false)
    private Integer animeTotalVoteCount;

    @Column(nullable = false)
    private Integer characterTotalVoteCount;
}