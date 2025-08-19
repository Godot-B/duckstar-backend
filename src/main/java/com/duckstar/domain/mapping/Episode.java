package com.duckstar.domain.mapping;

import com.duckstar.domain.Anime;
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
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_episode_e",
                        columnNames = "episode_number"),
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
}
