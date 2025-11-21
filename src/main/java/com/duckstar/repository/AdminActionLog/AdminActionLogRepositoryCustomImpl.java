package com.duckstar.repository.AdminActionLog;

import com.duckstar.domain.QQuarter;
import com.duckstar.domain.QWeek;
import com.duckstar.domain.mapping.QAdminActionLog;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.duckstar.web.dto.admin.AdminLogDto.*;

@Repository
@RequiredArgsConstructor
public class AdminActionLogRepositoryCustomImpl implements AdminActionLogRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QAdminActionLog adminActionLog = QAdminActionLog.adminActionLog;
    private final QWeek week = QWeek.week;
    private final QQuarter quarter = QQuarter.quarter;

    @Override
    public List<IpManagementLogDto> getIpManagementLogDtos(Pageable pageable) {
        int pageSize = pageable.getPageSize();

        return queryFactory.select(
                        Projections.constructor(
                                IpManagementLogDto.class,
                                adminActionLog.id,
                                adminActionLog.member.id,
                                adminActionLog.member.profileImageUrl,
                                adminActionLog.member.nickname,
                                week.id,
                                quarter.yearValue,
                                quarter.quarterValue,
                                week.weekValue,
                                adminActionLog.targetIpHash,
                                adminActionLog.adminTaskType,
                                adminActionLog.reason,
                                adminActionLog.isUndoable,
                                adminActionLog.createdAt
                        )
                ).from(adminActionLog)
                .leftJoin(adminActionLog.week, week)
                .leftJoin(week.quarter, quarter)
                .orderBy(adminActionLog.createdAt.desc())
                .offset((long) pageable.getPageNumber() * (pageSize - 1))
                .limit(pageSize)
                .fetch();
    }
}
