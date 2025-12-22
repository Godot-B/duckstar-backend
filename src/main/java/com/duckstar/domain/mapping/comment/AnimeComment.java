package com.duckstar.domain.mapping.comment;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Member;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.surveyVote.SurveyCandidate;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import com.duckstar.domain.mapping.weeklyVote.EpisodeStar;
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
    @JoinColumn(name = "episode_star_id")
    private EpisodeStar episodeStar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_candidate_id")
    private SurveyCandidate surveyCandidate;

    protected AnimeComment(
            Anime anime,
            Episode episode,
            Member member,
            Boolean isUserTaggedEp,
            Integer voteCount,
            String attachedImageUrl,
            String body
    ) {
        super(
                anime.getId(),
                episode,
                member,
                isUserTaggedEp,
                voteCount,
                attachedImageUrl,
                body
        );
        this.anime = anime;
    }

    public static AnimeComment create(
            Anime anime,
            Episode episode,
            Member member,
            Boolean isUserTaggedEp,
            Integer voteCount,
            String attachedImageUrl,
            String body
    ) {
        return new AnimeComment(
                anime,
                episode,
                member,
                isUserTaggedEp,
                voteCount,
                attachedImageUrl,
                body
        );
    }

    public void setEpisodeStar(EpisodeStar episodeStar) {
        this.episodeStar = episodeStar;
    }

    public void setSurveyCandidate(SurveyCandidate surveyCandidate) { this.surveyCandidate = surveyCandidate; }

    public void setStatus(CommentStatus status) {
        if (status == CommentStatus.DELETED
            || status == CommentStatus.ADMIN_DELETED) {

            episodeStar = null;
        }
        this.status = status;
    }
}
