package com.duckstar.schedule;

import com.duckstar.service.AnimeService.AnimeCommandService;
import com.duckstar.service.SurveyService;
import com.duckstar.service.WeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleHandler {
    private static final int ANCHOR_HOUR = 18;

    private final WeekService weekService;
    private final ScheduleState scheduleState;
    private final AnimeCommandService animeCommandService;
    private final SurveyService surveyService;

    // 매 분마다 시작 or 종영 체크
    @Scheduled(cron = "0 * * * * *")
    public void checkAnimeStatus() {
        if (scheduleState.isAdminMode()) {
            return;
        }

        animeCommandService.updateStatesByWindows();
    }

    // ⚠️확장할 때 주의 : 매 1시간마다 서베이 상태 체크
    @Scheduled(cron = "0 0 * * * *")
    public void checkSurveyStatus() {
        surveyService.updateStatus();
    }

    // 매주 월요일 18시
    @Scheduled(cron = "0 0 18 * * MON")
    public void startNewWeek() {

        // ** 어드민 모드와의 상태 충돌 해소 플래그 없음 주의

        LocalDateTime nowWithAnchorHour = LocalDateTime.of(
                LocalDate.now(),
                LocalTime.of(ANCHOR_HOUR, 0)
        );

        LocalDateTime lastWeekStartedAt = nowWithAnchorHour.minusWeeks(1);

        // 새로운 주 생성
        weekService.setupWeeklyVote(lastWeekStartedAt, nowWithAnchorHour);
    }

    /**
     * 랭킹 업로드 자동화
     * 일단 보류
     */
//    // 매주 화요일 13시 30분
//    @Scheduled(cron = "0 30 13 * * TUE")
//    public void handleWeeklySchedule() {
//        scheduleState.startRunning();
//
//        try {
//            LocalDateTime newWeekStartAt = LocalDateTime.of(
//                    LocalDate.now().minusDays(1),
//                    LocalTime.of(18, 0)
//            );
//            closeOldWeek(newWeekStartAt);
//        } finally {
//            scheduleState.stopRunning();
//        }
//    }
}