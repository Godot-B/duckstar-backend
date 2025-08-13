package com.duckstar.repository.Week;

import com.duckstar.domain.QQuarter;
import com.duckstar.domain.QWeek;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WeekRepositoryCustomImpl implements WeekRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QWeek week = QWeek.week;

    @Override
    public Optional<Long> findWeekIdByYQW(int yearValue, int quarterValue, int weekValue) {

        Long weekId = queryFactory.select(week.id)
                .from(week)
                .where(
                        week.quarter.yearValue.eq(yearValue),
                        week.quarter.quarterValue.eq(quarterValue),
                        week.weekValue.eq(weekValue)
                )
                .fetchOne();

        return Optional.ofNullable(weekId);
    }
}
