package com.duckstar.schedule;

import com.duckstar.service.EpisodeService.EpisodeCommandServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.duckstar.util.QuarterUtil.*;
import static com.duckstar.util.QuarterUtil.getThisWeekRecord;
import static com.duckstar.util.QuarterUtil.getThisWeekStartedAt;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class EpisodeStartupInitializer {

    private final EpisodeCommandServiceImpl episodeCommandService;
    private final ScheduleHandler scheduleHandler;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        episodeCommandService.updateAllEpisodeStates();

        // 이번 주 Week 없다면 생성
        LocalDateTime now = LocalDateTime.now();
        YQWRecord thisWeekRecord = getThisWeekRecord(now);
        scheduleHandler.getOrCreateQuarterAndWeek(
                true,
                thisWeekRecord,
                getThisWeekStartedAt(now)
        );

        // 지난 주 Week 없다면 생성
        LocalDateTime nowMinusWeek = LocalDateTime.now().minusWeeks(1);
        YQWRecord lastWeekRecord = getThisWeekRecord(nowMinusWeek);
        scheduleHandler.getOrCreateQuarterAndWeek(
                true,
                lastWeekRecord,
                getThisWeekStartedAt(nowMinusWeek)
        );
    }
}