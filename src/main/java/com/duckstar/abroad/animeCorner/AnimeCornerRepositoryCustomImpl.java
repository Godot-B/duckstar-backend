package com.duckstar.abroad.animeCorner;

import com.duckstar.abroad.aniLab.Anilab;
import com.duckstar.abroad.aniLab.QAnilab;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AnimeCornerRepositoryCustomImpl implements AnimeCornerRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QAnimeCorner animeCorner = QAnimeCorner.animeCorner;

    @Override
    public List<AnimeCorner> findAllByWeek_Id(Long weekId, int offset, int limit) {
        return queryFactory.selectFrom(animeCorner)
                .where(animeCorner.week.id.eq(weekId))
                .orderBy(animeCorner.rank.asc(), animeCorner.title.asc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }
}