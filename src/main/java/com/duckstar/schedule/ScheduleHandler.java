package com.duckstar.schedule;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.AnimeStatus;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.service.AnimeService;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final AnimeService animeService;
    private final ScheduleState scheduleState;

    // 매 분마다 시작 or 종영 체크
    @Scheduled(fixedRate = 60000)
    public void checkAnimeStatus() {
        if (scheduleState.isAdminMode()) {
            return;
        }

        animeService.updateAnimeStatusByMinute();
    }

//    // 매주 화요일 13시 30분
//    @Scheduled(cron = "0 30 13 * * TUE")
//    public void handleWeeklySchedule() {
//        scheduleState.startRunning();
//
//        try {
//            LocalDateTime newWeekStartAt = LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 0));
//            runSchedule(newWeekStartAt);
//        } finally {
//            scheduleState.stopRunning();
//        }
//    }

    public void runSchedule(LocalDateTime newWeekStartAt) {
        log.info("✈️ 주간 마무리 작업 시작 - {}", newWeekStartAt.format(FORMATTER));
        try {
            Week lastWeek = weekService.getWeekByTime(newWeekStartAt.minusWeeks(1));
            Long lastWeekId = lastWeek.getId();

            YQWRecord newWeekRecord = getThisWeekRecord(newWeekStartAt);
            weekService.setupWeeklyVote(lastWeekId, newWeekStartAt, newWeekRecord);  // 1. 새로운 주 생성

            Week secondLastWeek = weekService.getWeekByTime(newWeekStartAt.minusWeeks(2));
            chartService.buildDuckstars(newWeekStartAt, lastWeekId, secondLastWeek.getId());  // 2. 지난 주 덕스타 결과 분석

            //TODO 3. 해외 순위 수집

            log.info("✅ 주간 마무리 작업 완료 - {}", newWeekStartAt.format(FORMATTER));
        } catch (Exception e) {
            log.error("❌ 주간 마무리 작업 실패 - {}", newWeekStartAt.format(FORMATTER), e);
        }
    }
}