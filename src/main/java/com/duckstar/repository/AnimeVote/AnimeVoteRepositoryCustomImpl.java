package com.duckstar.repository.AnimeVote;

import com.duckstar.domain.QAnime;
import com.duckstar.domain.mapping.QAnimeVote;
import com.duckstar.web.dto.VoteResponseDto.AnimeBallotDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AnimeVoteRepositoryCustomImpl implements AnimeVoteRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QAnimeVote animeVote = QAnimeVote.animeVote;
    private final QAnime anime = QAnime.anime;

    @Override
    public List<AnimeBallotDto> getVoteHistoryBySubmissionId(Long submissionId) {
        return queryFactory.select(
                        Projections.constructor(
                                AnimeBallotDto.class,
                                animeVote.ballotType,
                                anime.id,
                                anime.mainThumbnailUrl,
                                anime.titleKor,
                                anime.medium
                        )
                )
                .from(animeVote)
                .join(animeVote.animeCandidate.anime, anime)
                .where(animeVote.weekVoteSubmission.id.eq(submissionId))
                .orderBy(animeVote.score.desc(), anime.titleKor.asc())
                .fetch();
    }
}
