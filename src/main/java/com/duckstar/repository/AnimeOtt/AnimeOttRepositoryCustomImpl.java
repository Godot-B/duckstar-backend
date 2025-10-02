package com.duckstar.repository.AnimeOtt;

import com.duckstar.domain.QOtt;
import com.duckstar.domain.mapping.QAnimeOtt;
import com.duckstar.web.dto.OttDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.duckstar.web.dto.AnimeResponseDto.*;

@Repository
@RequiredArgsConstructor
public class AnimeOttRepositoryCustomImpl implements AnimeOttRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QAnimeOtt animeOtt = QAnimeOtt.animeOtt;
    private final QOtt ott = QOtt.ott;

    @Override
    public List<OttDto> getOttDtosByAnimeId(Long animeId) {
        return queryFactory.select(
                        Projections.constructor(
                                OttDto.class,
                                ott.type,
                                animeOtt.watchUrl
                        )
                )
                .from(animeOtt)
                .join(animeOtt.ott, ott)
                .where(animeOtt.anime.id.eq(animeId))
                .orderBy(ott.typeOrder.asc())
                .fetch();
    }
}
