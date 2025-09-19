package com.duckstar.service;

import com.duckstar.domain.Week;
import com.duckstar.repository.Week.WeekRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ChartServiceTest {
    @Autowired
    private ChartService chartService;

    @Autowired
    private WeekRepository weekRepository;

    @Test
    public void buildChartTest() throws Exception {
        Week lastWeek = weekRepository.findWeekById(1L).orElseThrow();
        Week secondLastWeek = null;
    }
}
