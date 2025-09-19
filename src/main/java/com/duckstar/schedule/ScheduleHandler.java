package com.duckstar.schedule;

import com.duckstar.domain.Week;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.service.ChartService;
import com.duckstar.service.WeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static com.duckstar.util.QuarterUtil.*;
import static com.duckstar.util.QuarterUtil.getThisWeekRecord;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleHandler {
    private final WeekRepository weekRepository;

    private final WeekService weekService;
    private final ChartService chartService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // 매주 일요일 22시
    @Scheduled(cron = "0 0 22 * * SUN")
    public void handleWeeklySchedule() {
        LocalDateTime now = LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 0));

        log.info("✈️ 주간 마무리 작업 시작 - {}", now.format(FORMATTER));
        try {
            Week lastWeek = weekRepository.findFirstByOrderByStartDateTimeDesc();
            YQWRecord record = getThisWeekRecord(now);

            weekService.setupWeeklyVote(now, lastWeek, record);  // 1. 새로운 주 생성

            Week secondLastWeek = weekRepository.findSecondByOrderByStartDateTimeDesc();
            chartService.buildDuckstars(now, lastWeek, secondLastWeek);  // 2. 지난 주 덕스타 결과 분석

            //TODO 3. 해외 순위 수집

            log.info("✅ 주간 마무리 작업 완료 - {}", now.format(FORMATTER));
        } catch (Exception e) {
            log.error("❌ 주간 마무리 작업 실패 - {}", now.format(FORMATTER), e);
        }
    }
}