package com.duckstar.domain.mapping.comment;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Member;
import com.duckstar.domain.mapping.Episode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("A")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnimeComment extends Comment {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id", nullable = false)
    private Anime anime;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id")
    private Episode episode;

    protected AnimeComment(
            Anime anime,
            Episode episode,
            Member member,
            Integer voteCount,
            String attachedImageUrl,
            String body
    ) {
        super(anime.getId(), member, voteCount, attachedImageUrl, body);
        this.anime = anime;
        this.episode = episode;
    }

    public static AnimeComment create(
            Anime anime,
            Episode episode,
            Member member,
            Integer voteCount,
            String attachedImageUrl,
            String body
    ) {
        return new AnimeComment(
                anime,
                episode,
                member,
                voteCount,
                attachedImageUrl,
                body
        );
    }
}
