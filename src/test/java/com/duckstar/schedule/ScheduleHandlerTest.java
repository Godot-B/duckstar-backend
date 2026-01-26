package com.duckstar.schedule;

import com.duckstar.domain.Quarter;
import com.duckstar.domain.Week;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ScheduleHandlerTest {
    @Autowired ScheduleHandler scheduleHandler;

    @Test
    @Transactional
    public void getSafeWeek_경계_테스트() throws Exception {
        // 23년 4분기 14주차 테스트
        LocalDateTime time = LocalDateTime
                .of(2023,12,25,
                        18,0);
        LocalDateTime nowWithAnchorHour = time.with(LocalTime.of(18, 0));

        Week week = scheduleHandler.getSafeWeekByTime(nowWithAnchorHour);
        Quarter quarter = week.getQuarter();

        assertThat(quarter.getYearValue()).isEqualTo(2023);
        assertThat(quarter.getQuarterValue()).isEqualTo(4);
        assertThat(week.getWeekValue()).isEqualTo(14);
        assertThat(week.getStartDateTime()).isEqualTo(nowWithAnchorHour);

        // 24년 1분기 1주차 테스트
        LocalDateTime time2 = LocalDateTime
                .of(2024,1,1,
                        18,0);
        LocalDateTime nowWithAnchorHour2 = time2.with(LocalTime.of(18, 0));

        Week week2 = scheduleHandler.getSafeWeekByTime(nowWithAnchorHour2);
        Quarter quarter2 = week2.getQuarter();

        assertThat(quarter2.getYearValue()).isEqualTo(2024);
        assertThat(quarter2.getQuarterValue()).isEqualTo(1);
        assertThat(week2.getWeekValue()).isEqualTo(1);
        assertThat(week2.getStartDateTime()).isEqualTo(nowWithAnchorHour2);
    }
}
