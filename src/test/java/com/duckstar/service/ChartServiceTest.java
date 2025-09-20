package com.duckstar.service;

import com.duckstar.domain.Week;
import com.duckstar.repository.Week.WeekRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

@SpringBootTest
public class ChartServiceTest {

    @Autowired ChartService chartService;
    @Autowired WeekRepository weekRepository;

    @Test
    @Rollback(false)
    @Disabled("수동 실행 전용")
    public void buildChartTest() throws Exception {
        long start = System.nanoTime();

        //=== 테스트 코드 ===//
        long weekId = (long) 4;
        Week lastWeek = weekRepository.findWeekById(weekId).orElse(null);

        chartService.buildDuckstars(lastWeek.getEndDateTime(), lastWeek.getId(), weekId - 1);

        long end = System.nanoTime();
        double elapsedSeconds = (end - start) / 1_000_000_000.0;
        System.out.println("⏱ Execution time: " + elapsedSeconds + " seconds");
    }
}
