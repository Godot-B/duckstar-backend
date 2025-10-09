package com.duckstar.abroad.aniLab;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AnilabRepositoryCustomImpl implements AnilabRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QAnilab anilab = QAnilab.anilab;

    @Override
    public List<Anilab> findAllByWeek_IdWithOverFetch(Long weekId, Pageable pageable) {
        int pageSize = pageable.getPageSize();

        return queryFactory.selectFrom(anilab)
                .where(anilab.week.id.eq(weekId))
                .orderBy(anilab.rank.asc(), anilab.title.asc())
                .offset((long) pageable.getPageNumber() * (pageSize - 1))
                .limit(pageSize)
                .fetch();
    }
}
