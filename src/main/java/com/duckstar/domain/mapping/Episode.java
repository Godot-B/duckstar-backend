package com.duckstar.domain.mapping;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Week;
import com.duckstar.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
                @Index(name = "idx_episode_s",
                        columnList = "scheduled_at"),
        }
)
public class Episode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id", nullable = false)
    private Anime anime;

    private Integer episodeNumber;

    //=== 결방, 변칙 편성 update API 마련 필요 ===//

    private Boolean isBreak;

    private Boolean isRescheduled;

    private LocalDateTime scheduledAt;

    private LocalDateTime nextEpScheduledAt;

    protected Episode(
            Anime anime,
            Integer episodeNumber,
            Boolean isBreak,
            Boolean isRescheduled,
            LocalDateTime scheduledAt,
            LocalDateTime nextEpScheduledAt
    ) {
        this.anime = anime;
        this.episodeNumber = episodeNumber;
        this.isBreak = isBreak;
        this.isRescheduled = isRescheduled;
        this.scheduledAt = scheduledAt;
        this.nextEpScheduledAt = nextEpScheduledAt;
    }

    public static Episode create(
            Anime anime,
            Integer episodeNumber,
            LocalDateTime scheduledAt,
            LocalDateTime nextEpScheduledAt
    ) {
        return new Episode(
                anime,
                episodeNumber,
                null,
                null,
                scheduledAt,
                nextEpScheduledAt
        );
    }
}
