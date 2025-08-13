package com.duckstar.repository.AnimeSeason;

import com.duckstar.domain.QSeason;
import com.duckstar.domain.mapping.QAnimeSeason;
import com.duckstar.web.dto.AnimeResponseDto.SeasonDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AnimeSeasonRepositoryCustomImpl implements AnimeSeasonRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QAnimeSeason animeSeason = QAnimeSeason.animeSeason;
    private final QSeason season = QSeason.season;

    @Override
    public List<SeasonDto> getSeasonDtosByAnimeId(Long animeId) {
        return queryFactory.select(
                        Projections.constructor(
                                SeasonDto.class,
                                season.yearValue,
                                season.type
                        )
                )
                .from(animeSeason)
                .join(season).on(season.id.eq(animeSeason.season.id))
                .where(animeSeason.anime.id.eq(animeId))
                .orderBy(season.yearValue.asc(), season.typeOrder.asc())
                .fetch();
    }
}
