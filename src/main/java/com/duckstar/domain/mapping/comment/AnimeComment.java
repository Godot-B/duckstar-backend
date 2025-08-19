package com.duckstar.domain.mapping.comment;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Member;
import com.duckstar.domain.enums.CommentStatus;
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

    protected AnimeComment(
            Member member,
            Integer voteCount,
            String attachedImageUrl,
            String body,
            Anime anime
    ) {
        super(member, voteCount, attachedImageUrl, body);
        this.anime = anime;
    }

    public static AnimeComment create(
            Member member,
            Integer voteCount,
            String attachedImageUrl,
            String body,
            Anime anime
    ) {
        return new AnimeComment(
                member,
                voteCount,
                attachedImageUrl,
                body,
                anime
        );
    }
}
