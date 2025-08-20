package com.duckstar.domain.mapping.comment;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("A")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
                @Index(name = "idx_anime_comment_ac",
                        columnList = "anime_id, created_at")
        }
)
public class AnimeComment extends Comment {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id", nullable = false)
    private Anime anime;

    protected AnimeComment(
            Anime anime,
            Member member,
            Integer voteCount,
            String attachedImageUrl,
            String body
    ) {
        super(member, voteCount, attachedImageUrl, body);
        this.anime = anime;
    }

    public static AnimeComment create(
            Anime anime,
            Member member,
            Integer voteCount,
            String attachedImageUrl,
            String body
    ) {
        return new AnimeComment(
                anime,
                member,
                voteCount,
                attachedImageUrl,
                body
        );
    }
}
