package com.duckstar.schedule;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Season;
import com.duckstar.domain.Week;
import com.duckstar.domain.mapping.AnimeCandidate;
import com.duckstar.domain.mapping.WeekVoteSubmission;
import com.duckstar.repository.AnimeCandidate.AnimeCandidateRepository;
import com.duckstar.repository.AnimeVote.AnimeVoteRepository;
import com.duckstar.repository.SeasonRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.repository.WeekVoteSubmissionRepository;
import com.duckstar.service.AnimeService;
import com.duckstar.service.ChartService;
import com.duckstar.service.WeekService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@Disabled("로컬 개발용 테스트")
@ActiveProfiles("test-db")
public class ScheduleHandlerTest {

    @Autowired
    private AnimeService animeService;
    @Autowired
    private SeasonRepository seasonRepository;
    @Autowired
    private WeekRepository weekRepository;
    @Autowired
    private AnimeCandidateRepository animeCandidateRepository;
    @Autowired
    private ChartService chartService;
    @Autowired
    private WeekService weekService;
    @Autowired
    private AnimeVoteRepository animeVoteRepository;
    @Autowired
    private WeekVoteSubmissionRepository weekVoteSubmissionRepository;
    @Autowired
    private ScheduleHandler scheduleHandler;

    @Test
    @Transactional
    @Rollback(false)
    public void saveCandidates() throws Exception {
//        long start = System.nanoTime();

        //given
        LocalDateTime time = LocalDateTime.of(2025, 9, 26, 19, 0);
        Season season = seasonRepository.findById(1L).get();
        Week week = weekRepository.findWeekById(18L).get();

        //when
        List<Anime> thisWeekCandidates = animeService.getAnimesForCandidate(season, time);

        List<AnimeCandidate> animeCandidates = thisWeekCandidates.stream()
                .map(anime -> AnimeCandidate.create(week, anime))
                .toList();
        animeCandidateRepository.saveAll(animeCandidates);

        //then

//        long end = System.nanoTime();
//        double elapsedSeconds = (end - start) / 1_000_000_000.0;
//        System.out.println("⏰ 실행 시간: " + elapsedSeconds + " seconds");
    }

    @Test
    @Rollback(false)
    public void buildChartTest() throws Exception {
        long start = System.nanoTime();

        //=== 테스트 코드 ===//
        long[] idList = { 2, 3, 4, 5, 17 };

        for (long id : idList) {
            Week lastWeek = weekRepository.findWeekById(id).orElse(null);
            Week secondLastWeek = weekService.getWeekByTime(lastWeek.getStartDateTime().minusWeeks(1));

            chartService.buildDuckstars(lastWeek.getEndDateTime(), lastWeek.getId(), secondLastWeek.getId());
        }

        long end = System.nanoTime();
        double elapsedSeconds = (end - start) / 1_000_000_000.0;
        System.out.println("⏱ Execution time: " + elapsedSeconds + " seconds");
    }


    @Test
    @Rollback(false)
    public void schduleTest() throws Exception {
        scheduleHandler.runSchedule(LocalDateTime.of(2025, 10, 3, 19, 0));
    }
}
