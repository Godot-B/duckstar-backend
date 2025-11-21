package com.duckstar.domain;

import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.WeekVoteStatus;
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
    @Column(columnDefinition = "varchar(10)")
    private WeekVoteStatus status;

    @Column(nullable = false)
    private Integer weekValue;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column(nullable = false)
    private Integer animeVotes = 0;

    @Column(nullable = false)
    private Integer animeVoterCount = 0;

    @Column(nullable = false)
    private Integer characterVotes = 0;

    @Column(nullable = false)
    private Integer characterVoterCount = 0;

    private Boolean announcePrepared = false;

    protected Week(
            Quarter quarter,
            WeekVoteStatus status,
            Integer weekValue,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        this.quarter = quarter;
        this.status = status;
        this.weekValue = weekValue;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public static Week create(
            Quarter quarter,
            Integer weekValue,
            LocalDateTime startDateTime
    ) {
        return new Week(
                quarter,
                WeekVoteStatus.CLOSED,
                weekValue,
                startDateTime,
                startDateTime.plusWeeks(1)
        );
    }

    public void closeVote() {
        if (status != WeekVoteStatus.CLOSED) {
            status = WeekVoteStatus.CLOSED;
        }
    }

    public void openVote() {
        if (status != WeekVoteStatus.OPEN) {
            status = WeekVoteStatus.OPEN;
        }
    }

    public void updateAnimeVotes(int animeVotes, int animeVoterCount) {
        this.animeVotes = animeVotes;
        this.animeVoterCount = animeVoterCount;
    }
}