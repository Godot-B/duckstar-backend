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
    public List<AnimeCorner> findAllByWeek_IdWithOverFetch(Long weekId, Pageable pageable) {
        int pageSize = pageable.getPageSize();

        return queryFactory.selectFrom(animeCorner)
                .where(animeCorner.week.id.eq(weekId))
                .orderBy(animeCorner.rank.asc(), animeCorner.title.asc())
                .offset((long) pageable.getPageNumber() * (pageSize - 1))
                .limit(pageSize)
                .fetch();
    }
}