//package com.duckstar.schedule;
//
//import com.duckstar.domain.Week;
//import com.duckstar.domain.mapping.Episode;
//import com.duckstar.repository.Episode.EpisodeRepository;
//import com.duckstar.repository.Week.WeekRepository;
//import com.duckstar.service.ChartService;
//import com.duckstar.temp.Temp;
//import com.duckstar.temp.TempRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@SpringBootTest
//@Disabled("로컬 개발용 테스트")
//@ActiveProfiles("test-db")
//public class TempTest {
//
//    @Autowired
//    private WeekRepository weekRepository;
//    @Autowired
//    private EpisodeRepository episodeRepository;
//    @Autowired
//    private TempRepository tempRepository;
//    @Autowired
//    private ChartService chartService;
//
//    @Test
//    @Transactional
////    @Rollback(false)
//    public void calculateRankManual() {
//        Week week = weekRepository.findById(22L).get();
//
//        List<Episode> lastEpisodes = episodeRepository
//                .findAllByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
//                        week.getStartDateTime(), week.getEndDateTime());
//
//        Map<Long, Episode> episodeMap = lastEpisodes.stream()
//                .collect(Collectors.toMap(
//                        e -> e.getAnime().getId(),
//                        e -> e
//                ));
//
//        List<Temp> temps = tempRepository.findAll();
//
//        Map<Episode, Temp> tempMap = temps.stream()
//                .collect(Collectors.toMap(
//                        t -> episodeMap.get(t.getAnimeId()),
//                        t -> t)
//                );
//
//        for (Map.Entry<Episode, Temp> entry : tempMap.entrySet()) {
//            Episode episode = entry.getKey();
//            Temp temp = entry.getValue();
//
//            episode.setStats(temp.getVoterCount(), temp.getStarList());
//        }
//        episodeRepository.flush();
//
//        chartService.buildDuckstars(week.getEndDateTime(), 22L, 21L);
//    }
//}
