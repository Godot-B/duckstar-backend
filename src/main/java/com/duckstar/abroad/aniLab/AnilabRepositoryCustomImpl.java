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
    public List<Anilab> findAllByWeek_Id(Long weekId, int offset, int limit) {
        return queryFactory.selectFrom(anilab)
                .where(anilab.week.id.eq(weekId))
                .orderBy(anilab.rank.asc(), anilab.title.asc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }
}
