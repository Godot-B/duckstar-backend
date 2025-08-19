package com.duckstar.domain.mapping.comment;

import com.duckstar.domain.Anime;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@DiscriminatorValue("A")
@Getter
public class AnimeComment extends Comment {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id", nullable = false)
    private Anime anime;
}
