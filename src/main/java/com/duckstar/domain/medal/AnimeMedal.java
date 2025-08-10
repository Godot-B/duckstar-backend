package com.duckstar.domain.medal;

import com.duckstar.domain.Anime;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("ANIME") // dtype 컬럼에 'ANIME'으로 저장됨
public class AnimeMedal extends Medal {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id")
    private Anime anime;
}