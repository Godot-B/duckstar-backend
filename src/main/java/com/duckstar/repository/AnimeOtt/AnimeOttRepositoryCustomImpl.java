package com.duckstar.repository.AnimeOtt;

import com.duckstar.domain.QOtt;
import com.duckstar.domain.mapping.QAnimeOtt;
import com.duckstar.web.dto.AnimeResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AnimeOttRepositoryCustomImpl implements AnimeOttRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QAnimeOtt animeOtt = QAnimeOtt.animeOtt;
    private final QOtt ott = QOtt.ott;

    @Override
    public List<AnimeResponseDto.OttDto> getOttDtosByAnimeId(Long animeId) {
        return queryFactory.select(
                        Projections.constructor(
                                AnimeResponseDto.OttDto.class,
                                ott.type,
                                animeOtt.watchUrl
                        )
                )
                .from(animeOtt)
                .join(ott).on(ott.id.eq(animeOtt.ott.id))
                .where(animeOtt.anime.id.eq(animeId))
                .orderBy(ott.typeOrder.asc())
                .fetch();
    }
}
