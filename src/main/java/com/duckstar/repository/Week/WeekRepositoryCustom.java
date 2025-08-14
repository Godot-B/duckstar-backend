package com.duckstar.repository.Week;

import java.util.Optional;

public interface WeekRepositoryCustom {
    Optional<Long> findQuarterIdByYQ(int yearValue, int quarterValue);
    Optional<Long> findWeekIdByYQW(int yearValue, int quarterValue, int weekValue);
}
