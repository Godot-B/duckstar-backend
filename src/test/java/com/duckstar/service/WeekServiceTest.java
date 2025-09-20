package com.duckstar.service;

import com.duckstar.domain.Week;
import com.duckstar.domain.enums.AnimeStatus;
import com.duckstar.domain.mapping.AnimeCandidate;
import com.duckstar.repository.AnimeCandidate.AnimeCandidateRepository;
import com.duckstar.repository.Week.WeekRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.duckstar.util.QuarterUtil.*;
import static com.duckstar.util.QuarterUtil.getThisWeekRecord;

@Tag("mysql")
@SpringBootTest
@ActiveProfiles("test-db")
public class WeekServiceTest {

    @Autowired WeekService weekService;
    @Autowired WeekRepository weekRepository;
    @Autowired AnimeCandidateRepository animeCandidateRepository;

    @Test
    @Transactional
    public void buildChartTest() throws Exception {
        //given
        LocalDateTime time = LocalDateTime.of(2025, 9, 21, 22, 0);
        Week lastWeek = weekRepository.findFirstByOrderByStartDateTimeDesc();
        YQWRecord record = getThisWeekRecord(time);

        //when
        weekService.setupWeeklyVote(time, lastWeek.getId(), record);

        //then
        Week thisWeek = weekRepository.findFirstByOrderByStartDateTimeDesc();
        Assertions.assertThat(thisWeek.getQuarter().getYearValue()).isEqualTo(2025);
        Assertions.assertThat(thisWeek.getQuarter().getQuarterValue()).isEqualTo(3);
        Assertions.assertThat(thisWeek.getWeekValue()).isEqualTo(13);

        List<AnimeCandidate> animeCandidates = animeCandidateRepository.findAllByWeek_Id(thisWeek.getId());
        Map<AnimeStatus, List<AnimeCandidate>> statusMap = animeCandidates.stream()
                .collect(Collectors.groupingBy(ac -> ac.getAnime().getStatus()));

        int size = animeCandidates.size();
        System.out.println("size: " + size);
        for (Map.Entry<AnimeStatus, List<AnimeCandidate>> entry : statusMap.entrySet()) {
            AnimeStatus animeStatus = entry.getKey();
            List<String> titleList = entry.getValue().stream()
                    .map(ac -> ac.getAnime().getTitleKor())
                    .toList();
            System.out.println("Anime Status: " + animeStatus);
            System.out.println("Title List: " + titleList);
        }
    }
}
